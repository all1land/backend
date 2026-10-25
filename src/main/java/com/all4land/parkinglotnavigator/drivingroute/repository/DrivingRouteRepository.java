package com.all4land.parkinglotnavigator.drivingroute.repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

// tb_road_link는 JPA 엔티티 없이 PostGIS/pgRouting 함수로만 조회한다. 좌표는 모두 WGS84(EPSG:4326).
// 링크는 시작 노드 → 종료 노드 방향으로만 갈 수 있다. 양방향 도로는 방향별 링크가 따로 있다.
@Repository
@RequiredArgsConstructor
public class DrivingRouteRepository {

    // pgRouting에 넘기는 간선 SQL. 경로 탐색 대상 링크만 쓰고, 비용은 주행 시간(초), 역방향은 갈 수 없다(-1).
    private static final String EDGES_SQL = "SELECT id, start_node_id AS source, end_node_id AS target,"
            + " travel_time_s AS cost, -1::FLOAT8 AS reverse_cost FROM tb_road_link WHERE is_routable";

    // 투영 후보 링크. 고속·도시고속국도(101, 102), 연결로(램프), 고가·지하차도(001, 002)는 출발지나 주차장에서 바로 들어갈 수
    // 없어서 제외한다. 나머지 조건은 도보 경로 투영과 같다: 도(degree) 단위로 후보를 넉넉히 고른 뒤 미터(geography) 거리로 다시 정렬한다.
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
                    FROM tb_road_link
                    WHERE is_routable
                      AND NOT is_ramp
                      AND road_rank_code NOT IN ('101', '102')
                      AND road_type_code NOT IN ('001', '002')
                    ORDER BY geom <-> p.pt
                    LIMIT :prefilter
                ) l
                ORDER BY distance_m
                LIMIT :limit
            ) c
            WHERE c.distance_m <= :maxDistance
            ORDER BY c.distance_m
            """;

    // 출발 후보 × 도착 후보 조합마다 최소 주행 시간을 구한다. 투영점은 가상 정점(-pid)으로 들어간다.
    private static final String COST_SQL = """
            SELECT start_vid, end_vid, agg_cost
            FROM pgr_withPointsCost(:edgesSql, :pointsSql, ARRAY[:startVids]::BIGINT[], ARRAY[:endVids]::BIGINT[],
                                    directed => true)
            """;

    // 출발/도착 투영점을 가상 정점(-1, -2)으로 넣어 링크 중간에서 출발·도착하게 한다.
    // 경로의 각 링크마다 들어간 위치(from_f)와 나온 위치(to_f)를 구해 실제로 지나간 부분만 잘라낸다.
    // 링크는 진행 방향으로만 지나가므로 항상 from_f < to_f다.
    // from_f/to_f: -1/-2(투영점)는 투영 위치, 시작 노드는 0, 종료 노드는 1
    private static final String PATH_SQL = """
            WITH path AS (
                SELECT seq, node, edge
                FROM pgr_withPoints(:edgesSql, :pointsSql, -1, -2, directed => true)
            ), step AS (
                SELECT seq, node AS from_node, lead(node) OVER (ORDER BY seq) AS to_node, edge
                FROM path
            ), part AS (
                SELECT s.seq,
                       l.length_m * (f.to_f - f.from_f) AS distance_m,
                       l.travel_time_s * (f.to_f - f.from_f) AS duration_s,
                       ST_LineSubstring(l.geom, f.from_f, f.to_f) AS geom
                FROM step s
                JOIN tb_road_link l ON l.id = s.edge
                CROSS JOIN LATERAL (
                    SELECT CASE s.from_node WHEN -1 THEN :startFraction WHEN -2 THEN :endFraction
                                            WHEN l.start_node_id THEN 0.0 ELSE 1.0 END AS from_f,
                           CASE s.to_node   WHEN -1 THEN :startFraction WHEN -2 THEN :endFraction
                                            WHEN l.start_node_id THEN 0.0 ELSE 1.0 END AS to_f
                ) f
                -- 들어간 위치와 나온 위치가 같으면 지나간 길이가 없다
                WHERE f.from_f <> f.to_f
            )
            SELECT distance_m, duration_s,
                   ARRAY(SELECT ARRAY[ST_X(d.geom), ST_Y(d.geom)] FROM ST_DumpPoints(part.geom) d ORDER BY d.path)
                       AS coordinates
            FROM part
            ORDER BY seq
            """;

    // 도(degree) 단위로 먼저 고르는 후보 수 = 최종 후보 수 × 이 값
    private static final int PREFILTER_FACTOR = 3;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 좌표에서 가까운 도로 링크들 위로 수선을 내린 투영점을 가까운 순으로 찾는다.
     *
     * @param lat 위도 (WGS84, EPSG:4326)
     * @param lng 경도 (WGS84, EPSG:4326)
     * @param limit 최대 후보 수
     * @param maxDistanceMeters 원래 좌표에서 투영점까지 허용하는 최대 거리(m)
     * @return 투영점 목록. 투영점 좌표도 WGS84(EPSG:4326). 허용 거리 안에 링크가 없으면 빈 목록
     */
    public List<SnappedPoint> snap(double lat, double lng, int limit, double maxDistanceMeters) {
        return jdbcTemplate.query(
                SNAP_SQL,
                Map.of(
                        "lat", lat,
                        "lng", lng,
                        "limit", limit,
                        "prefilter", limit * PREFILTER_FACTOR,
                        "maxDistance", maxDistanceMeters),
                (rs, rowNum) -> new SnappedPoint(
                        rs.getLong("link_id"),
                        rs.getDouble("fraction"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng"),
                        rs.getDouble("distance_m")));
    }

    /**
     * 출발 후보와 도착 후보의 모든 조합에 대해 최소 주행 시간을 구한다.
     *
     * @param starts 출발 투영점 후보 ({@link #snap} 결과)
     * @param ends 도착 투영점 후보 ({@link #snap} 결과)
     * @return 갈 수 있는 조합별 주행 시간. 갈 수 없는 조합은 빠진다
     */
    public List<DriveCost> findDriveCosts(List<SnappedPoint> starts, List<SnappedPoint> ends) {
        List<SnappedPoint> points = new ArrayList<>(starts);
        points.addAll(ends);
        // 가상 정점 번호: 출발 후보는 -1 ~ -n, 도착 후보는 -(n+1) ~ -(n+m)
        List<Long> startVids =
                LongStream.rangeClosed(1, starts.size()).map(i -> -i).boxed().toList();
        List<Long> endVids = LongStream.rangeClosed(starts.size() + 1, points.size())
                .map(i -> -i)
                .boxed()
                .toList();
        return jdbcTemplate.query(
                COST_SQL,
                Map.of(
                        "edgesSql",
                        EDGES_SQL,
                        "pointsSql",
                        pointsSql(points),
                        "startVids",
                        startVids,
                        "endVids",
                        endVids),
                (rs, rowNum) -> new DriveCost(
                        (int) (-rs.getLong("start_vid") - 1),
                        (int) (-rs.getLong("end_vid") - 1 - starts.size()),
                        rs.getDouble("agg_cost")));
    }

    /**
     * 두 투영점 사이의 최단 시간 차량 경로를 지나가는 링크 순서대로 반환한다.
     *
     * @param start 출발 투영점 ({@link #snap} 결과)
     * @param end 도착 투영점 ({@link #snap} 결과)
     * @return 링크별 구간. 좌표는 WGS84(EPSG:4326), [경도, 위도] 순서. 경로가 없거나 두 투영점이 같으면 빈 목록
     */
    public List<PathPart> findPath(SnappedPoint start, SnappedPoint end) {
        return jdbcTemplate.query(
                PATH_SQL,
                Map.of(
                        "edgesSql", EDGES_SQL,
                        "pointsSql", pointsSql(List.of(start, end)),
                        "startFraction", start.fraction(),
                        "endFraction", end.fraction()),
                (rs, rowNum) -> new PathPart(rs.getDouble("distance_m"), rs.getDouble("duration_s"), coordinates(rs)));
    }

    // pgRouting에 넘기는 투영점 SQL. i번째 점(0부터)은 pid i+1, 가상 정점 -(i+1)이 된다.
    // 값은 모두 snap 결과의 숫자라 문자열로 이어 붙여도 SQL 주입 위험이 없다.
    private static String pointsSql(List<SnappedPoint> points) {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            SnappedPoint point = points.get(i);
            rows.add(String.format(
                    Locale.ROOT,
                    "SELECT %d AS pid, %d::BIGINT AS edge_id, %s::FLOAT8 AS fraction",
                    i + 1,
                    point.linkId(),
                    Double.toString(point.fraction())));
        }
        return String.join(" UNION ALL ", rows);
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
     * @param startIndex 출발 후보 목록에서의 위치
     * @param endIndex 도착 후보 목록에서의 위치
     * @param seconds 최소 주행 시간(초)
     */
    public record DriveCost(int startIndex, int endIndex, double seconds) {}

    /**
     * @param distanceMeters 구간 거리(m)
     * @param durationSeconds 구간 주행 시간(초). 제한속도 기준
     * @param coordinates 구간 선형 좌표열. WGS84, [경도, 위도] 순서
     */
    public record PathPart(double distanceMeters, double durationSeconds, List<double[]> coordinates) {}
}
