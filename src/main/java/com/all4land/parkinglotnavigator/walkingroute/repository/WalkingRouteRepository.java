package com.all4land.parkinglotnavigator.walkingroute.repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

// tb_walk_link는 JPA 엔티티 없이 PostGIS/pgRouting 함수로만 조회한다. 좌표는 모두 WGS84(EPSG:4326).
@Repository
@RequiredArgsConstructor
public class WalkingRouteRepository {

    // pgRouting에 넘기는 간선 SQL. 보행자 통행 가능(링크 유형 코드 첫 자리 = 1)한 링크만 탐색한다.
    // 바인드 파라미터로 넘기므로 작은따옴표를 이스케이프하지 않는다.
    private static final String EDGES_SQL =
            "SELECT id, start_node_id AS source, end_node_id AS target, length_m AS cost"
                    + " FROM tb_walk_link WHERE LEFT(link_type_code, 1) = '1'";

    // geom <-> 는 도(degree) 단위 거리라 위도에 따라 경도 방향이 왜곡된다.
    // 후보 10개를 도 단위로 빠르게 고른 뒤 미터(geography) 거리로 다시 정렬한다.
    // 자기 자신으로 돌아오는 링크는 투영 위치(fraction)로 진행 방향을 알 수 없어 제외한다.
    private static final String SNAP_SQL = """
            SELECT c.link_id, c.fraction, ST_Y(c.snapped) AS lat, ST_X(c.snapped) AS lng, c.distance_m
            FROM (SELECT ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS pt) p
            CROSS JOIN LATERAL (
                SELECT l.id AS link_id,
                       ST_LineLocatePoint(l.geom, p.pt) AS fraction,
                       ST_LineInterpolatePoint(l.geom, ST_LineLocatePoint(l.geom, p.pt)) AS snapped,
                       ST_Distance(p.pt::geography, l.geom::geography) AS distance_m
                FROM (
                    SELECT id, geom
                    FROM tb_walk_link
                    WHERE LEFT(link_type_code, 1) = '1' AND start_node_id <> end_node_id
                    ORDER BY geom <-> p.pt
                    LIMIT 10
                ) l
                ORDER BY distance_m
                LIMIT 1
            ) c
            """;

    // 출발/도착 투영점을 가상 정점(-1, -2)으로 넣어 링크 중간에서 출발·도착하게 한다.
    // 경로의 각 링크마다 들어간 위치(from_f)와 나온 위치(to_f)를 구해 실제로 지나간 부분만 잘라낸다.
    // 링크 geom은 시작 노드 → 종료 노드 방향으로 저장돼 있어서, 종료 노드 쪽에서 들어간 링크(from_f > to_f)는
    // 좌표 순서를 뒤집어 걷는 순서와 맞춘다.
    // from_f/to_f: -1/-2(투영점)는 투영 위치, 시작 노드는 0, 종료 노드는 1
    private static final String PATH_SQL = """
            WITH path AS (
                SELECT seq, node, edge
                FROM pgr_withPoints(
                    :edgesSql,
                    format('SELECT 1 AS pid, %s::BIGINT AS edge_id, %s::FLOAT8 AS fraction UNION ALL SELECT 2, %s, %s',
                           :startLinkId, :startFraction, :endLinkId, :endFraction),
                    -1, -2, directed => false)
            ), step AS (
                SELECT seq, node AS from_node, lead(node) OVER (ORDER BY seq) AS to_node, edge
                FROM path
            ), part AS (
                SELECT s.seq, l.length_m * abs(f.to_f - f.from_f) AS distance_m,
                       CASE WHEN f.from_f <= f.to_f THEN ST_LineSubstring(l.geom, f.from_f, f.to_f)
                            ELSE ST_Reverse(ST_LineSubstring(l.geom, f.to_f, f.from_f)) END AS geom
                FROM step s
                JOIN tb_walk_link l ON l.id = s.edge
                CROSS JOIN LATERAL (
                    SELECT CASE s.from_node WHEN -1 THEN :startFraction WHEN -2 THEN :endFraction
                                            WHEN l.start_node_id THEN 0.0 ELSE 1.0 END AS from_f,
                           CASE s.to_node   WHEN -1 THEN :startFraction WHEN -2 THEN :endFraction
                                            WHEN l.start_node_id THEN 0.0 ELSE 1.0 END AS to_f
                ) f
                -- 들어간 위치와 나온 위치가 같으면 지나간 길이가 없다 (출발지 = 도착지 등)
                WHERE f.from_f <> f.to_f
            )
            SELECT distance_m,
                   ARRAY(SELECT ARRAY[ST_X(d.geom), ST_Y(d.geom)] FROM ST_DumpPoints(part.geom) d ORDER BY d.path)
                       AS coordinates
            FROM part
            ORDER BY seq
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 좌표에서 가장 가까운 보행 링크 위로 수선을 내린 투영점을 찾는다.
     *
     * @param lat 위도 (WGS84, EPSG:4326)
     * @param lng 경도 (WGS84, EPSG:4326)
     * @return 투영점. 투영점 좌표도 WGS84(EPSG:4326). 보행 링크가 하나도 없으면 empty
     */
    public Optional<SnappedPoint> snap(double lat, double lng) {
        return jdbcTemplate
                .query(
                        SNAP_SQL,
                        Map.of("lat", lat, "lng", lng),
                        (rs, rowNum) -> new SnappedPoint(
                                rs.getLong("link_id"),
                                rs.getDouble("fraction"),
                                rs.getDouble("lat"),
                                rs.getDouble("lng"),
                                rs.getDouble("distance_m")))
                .stream()
                .findFirst();
    }

    /**
     * 두 투영점 사이의 최단 도보 경로를 지나가는 링크 순서대로 반환한다.
     *
     * @param start 출발 투영점 ({@link #snap} 결과)
     * @param end 도착 투영점 ({@link #snap} 결과)
     * @return 링크별 구간. 좌표는 WGS84(EPSG:4326), [경도, 위도] 순서. 경로가 없으면 빈 목록
     */
    public List<PathPart> findPath(SnappedPoint start, SnappedPoint end) {
        return jdbcTemplate.query(
                PATH_SQL,
                Map.of(
                        "edgesSql", EDGES_SQL,
                        "startLinkId", start.linkId(),
                        "startFraction", start.fraction(),
                        "endLinkId", end.linkId(),
                        "endFraction", end.fraction()),
                (rs, rowNum) -> new PathPart(rs.getDouble("distance_m"), coordinates(rs)));
    }

    private static List<double[]> coordinates(ResultSet rs) throws SQLException {
        Array array = rs.getArray("coordinates");
        List<double[]> coordinates = new ArrayList<>();
        for (Object point : (Object[]) array.getArray()) {
            Object[] lngLat = (Object[]) point;
            coordinates.add(new double[] {((Number) lngLat[0]).doubleValue(), ((Number) lngLat[1]).doubleValue()});
        }
        return coordinates;
    }

    /**
     * @param linkId 투영된 링크 ID
     * @param fraction 링크 위 투영 위치 (0=시작 노드, 1=종료 노드)
     * @param lat 투영점 위도 (WGS84)
     * @param lng 투영점 경도 (WGS84)
     * @param distanceMeters 원래 좌표에서 투영점까지 거리(m)
     */
    public record SnappedPoint(long linkId, double fraction, double lat, double lng, double distanceMeters) {}

    /**
     * @param distanceMeters 구간 거리(m)
     * @param coordinates 구간 선형 좌표열. WGS84, [경도, 위도] 순서
     */
    public record PathPart(double distanceMeters, List<double[]> coordinates) {}
}
