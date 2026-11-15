package com.all4land.parkinglotnavigator.domain.route.repository;

import com.all4land.parkinglotnavigator.domain.route.DrivingRouteProperties;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// tb_road_link는 JPA 엔티티 없이 PostGIS/pgRouting 함수로만 조회한다. 좌표는 모두 WGS84(EPSG:4326).
// 링크는 시작 노드 → 종료 노드 방향으로만 갈 수 있다. 양방향 도로는 방향별 링크가 따로 있다.
@Repository
@Transactional(readOnly = true)
public class DrivingRouteRepository {

    // pgRouting 함수는 결과 행 수를 실제(수십 행)보다 크게 추정해서, 반복 실행되는 쿼리에 범용 실행 계획이 쓰이면 비용이 부풀어
    // 매번 JIT 컴파일이 일어난다(실행 약 10ms에 컴파일 약 100ms). 경로 탐색 쿼리 전에 현재 트랜잭션에서만 JIT를 끈다.
    private static final String DISABLE_JIT_SQL = "SET LOCAL jit = off";

    // 링크를 끝까지 지나는 데 걸리는 보정 주행 시간(초) = 제한속도 주행 시간 / 속도 계수 + (교차로 안 연결 링크면) 신호 대기 시간.
    // 경로 비용(간선 SQL)과 구간 소요 시간(PATH_SQL)에 같은 식을 써서, 고른 경로와 보여 주는 시간이 어긋나지 않게 한다.
    private static final String LINK_SECONDS_SQL =
            "(%1$stravel_time_s / %2$s + CASE WHEN %1$sis_intersection_internal THEN %3$s ELSE 0 END)";

    /** 회전 제한을 어기는 경로에 더하는 비용(초). 다른 길이 없을 때만 이 비용을 내고 금지된 회전을 지나간다. */
    public static final double TURN_RESTRICTION_PENALTY_SECONDS = 1e9;

    // 경로 탐색에 쓰는 회전 제한. 금지 유형(101 좌회전, 102 직진, 103 우회전, 003 회전 금지, 002 버스만 회전)만 쓴다.
    // link_path는 진입 링크부터 교차로 안 연결 링크를 거쳐 진출 링크까지의 링크 순서다(V14에서 계산).
    // 시간제 운영(operation_code=1)도 시간 정보가 없어 항상 금지로 본다. 현재 적재 범위에는 시간제가 없다.
    private static final String PROHIBITED_TURN_CONDITION =
            "link_path IS NOT NULL AND turn_type_code IN ('101', '102', '103', '003', '002')";

    // pgRouting에 넘기는 회전 제한 SQL
    private static final String RESTRICTIONS_SQL = String.format(
            Locale.ROOT,
            "SELECT link_path AS path, %s::FLOAT8 AS cost FROM tb_road_turn_restriction WHERE %s",
            Double.toString(TURN_RESTRICTION_PENALTY_SECONDS),
            PROHIBITED_TURN_CONDITION);

    // 회전 제한 경로에 들어 있는 링크
    private static final String RESTRICTED_LINKS_SQL =
            "SELECT DISTINCT unnest(link_path) AS link_id FROM tb_road_turn_restriction WHERE "
                    + PROHIBITED_TURN_CONDITION;

    // 투영 후보 링크. 고속·도시고속국도(101, 102), 연결로(램프), 고가·지하차도(001, 002)는 출발지나 주차장에서 바로 들어갈 수
    // 없어서 제외한다. 교차로 안 연결 링크도 제외한다. 교차로 한가운데에서 출발하면 진입 링크를 거치지 않아 회전 제한을 피해 가게 된다.
    // 나머지 조건은 도보 경로 투영과 같다: 도(degree) 단위로 후보를 넉넉히 고른 뒤 미터(geography) 거리로 다시 정렬한다.
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
                      AND NOT is_intersection_internal
                    ORDER BY geom <-> p.pt
                    LIMIT :prefilter
                ) l
                ORDER BY distance_m
                LIMIT :limit
            ) c
            WHERE c.distance_m <= :maxDistance
            ORDER BY c.distance_m
            """;

    // 회전 제한을 지키는 최소 주행 시간은 회전 제한을 지원하는 비용 전용 함수가 없어 경로를 구한 뒤 마지막 누적 비용을 쓴다.
    //
    // 투영점을 여러 개 한 번에 넣으면 pgRouting이 투영점마다 링크를 쪼개는데, 쪼개진 링크가 회전 제한 경로에 들어 있으면
    // 금지 경로와 링크 순서가 맞지 않아 회전 제한이 무시된다. 그래서 도착 후보를 둘로 나눠 계산한다.
    //   - 회전 제한 경로 링크 위에 없는 도착 후보: 출발 후보마다 한 번에 계산한다 (BATCH_COST_SQL)
    //   - 회전 제한 경로 링크 위의 도착 후보: 조합마다 출발·도착 투영점 2개만 넣어 계산한다 (PAIR_COST_SQL)
    // 출발 후보는 어느 쪽이든 한 쿼리에 하나씩만 넣는다. 두 방식 모두 경로 SQL(PATH_SQL)과 같은 비용을 낸다.

    // 출발 후보마다, 회전 제한 경로 링크 위에 없는 도착 후보 전부를 한 번에 계산한다.
    // 투영점 번호: 출발 후보는 pid 1(가상 정점 -1), i번째 도착 후보(1부터)는 pid i+1(가상 정점 -(i+1)).
    private static final String BATCH_COST_SQL = """
            WITH s AS (
                SELECT link_id, fraction, idx
                FROM unnest(ARRAY[:startLinkIds]::BIGINT[], ARRAY[:startFractions]::FLOAT8[])
                     WITH ORDINALITY AS t(link_id, fraction, idx)
            ), e AS (
                SELECT string_agg(format(' UNION ALL SELECT %s, %s, %s', idx + 1, link_id, fraction), '' ORDER BY idx)
                           AS points_sql,
                       array_agg(-(idx + 1) ORDER BY idx) AS vids
                FROM unnest(ARRAY[:endLinkIds]::BIGINT[], ARRAY[:endFractions]::FLOAT8[])
                     WITH ORDINALITY AS t(link_id, fraction, idx)
            )
            SELECT s.idx - 1 AS start_index, -x.end_vid - 2 AS end_index, x.agg_cost
            FROM s
            CROSS JOIN e
            CROSS JOIN LATERAL (
                SELECT end_vid, max(agg_cost) AS agg_cost
                FROM pgr_trsp_withPoints(
                    :edgesSql, :restrictionsSql,
                    format('SELECT 1 AS pid, %s::BIGINT AS edge_id, %s::FLOAT8 AS fraction', s.link_id, s.fraction)
                        || e.points_sql,
                    -1, e.vids, directed => true, details => false)
                GROUP BY end_vid
            ) x
            """;

    // 출발 후보 × 도착 후보 조합마다 출발·도착 투영점 2개만 넣어 계산한다.
    private static final String PAIR_COST_SQL = """
            WITH s AS (
                SELECT link_id, fraction, idx
                FROM unnest(ARRAY[:startLinkIds]::BIGINT[], ARRAY[:startFractions]::FLOAT8[])
                     WITH ORDINALITY AS t(link_id, fraction, idx)
            ), e AS (
                SELECT link_id, fraction, idx
                FROM unnest(ARRAY[:endLinkIds]::BIGINT[], ARRAY[:endFractions]::FLOAT8[])
                     WITH ORDINALITY AS t(link_id, fraction, idx)
            )
            SELECT s.idx - 1 AS start_index, e.idx - 1 AS end_index, x.agg_cost
            FROM s
            CROSS JOIN e
            CROSS JOIN LATERAL (
                SELECT max(agg_cost) AS agg_cost
                FROM pgr_trsp_withPoints(
                    :edgesSql, :restrictionsSql,
                    format('SELECT 1 AS pid, %s::BIGINT AS edge_id, %s::FLOAT8 AS fraction UNION ALL SELECT 2, %s, %s',
                           s.link_id, s.fraction, e.link_id, e.fraction),
                    -1, -2, directed => true, details => false)
            ) x
            WHERE x.agg_cost IS NOT NULL
            """;

    // 출발/도착 투영점을 가상 정점(-1, -2)으로 넣어 링크 중간에서 출발·도착하게 한다.
    // 경로의 각 링크마다 들어간 위치(from_f)와 나온 위치(to_f)를 구해 실제로 지나간 부분만 잘라낸다.
    // 링크는 진행 방향으로만 지나가므로 항상 from_f < to_f다.
    // from_f/to_f: -1/-2(투영점)는 투영 위치, 시작 노드는 0, 종료 노드는 1
    private static final String PATH_SQL = """
            WITH path AS (
                SELECT seq, node, edge
                FROM pgr_trsp_withPoints(:edgesSql, :restrictionsSql, :pointsSql, -1, -2,
                                         directed => true, details => false)
            ), step AS (
                SELECT seq, node AS from_node, lead(node) OVER (ORDER BY seq) AS to_node, edge
                FROM path
            ), part AS (
                SELECT s.seq,
                       l.length_m * (f.to_f - f.from_f) AS distance_m,
                       (l.travel_time_s / :speedFactor
                        + CASE WHEN l.is_intersection_internal THEN :intersectionDelay ELSE 0 END)
                           * (f.to_f - f.from_f) AS duration_s,
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
    private final double speedFactor;
    private final double intersectionDelaySeconds;

    // 회전 제한 경로에 들어 있는 링크. 적재 후 바뀌지 않아 처음 쓸 때 한 번 읽어 둔다.
    private volatile Set<Long> restrictedLinkIds;

    // pgRouting에 넘기는 간선 SQL. 경로 탐색 대상 링크만 쓰고, 비용은 보정 주행 시간(초), 역방향은 갈 수 없다(-1).
    // pgRouting이 문자열로 받아 실행해서 바인딩 변수를 쓸 수 없어, 설정값(숫자)을 넣어 한 번 만들어 둔다.
    private final String edgesSql;

    public DrivingRouteRepository(NamedParameterJdbcTemplate jdbcTemplate, DrivingRouteProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.speedFactor = properties.speedFactor();
        this.intersectionDelaySeconds = properties.intersectionDelaySeconds();
        this.edgesSql = "SELECT id, start_node_id AS source, end_node_id AS target, "
                + String.format(
                        Locale.ROOT,
                        LINK_SECONDS_SQL,
                        "",
                        Double.toString(speedFactor),
                        Double.toString(intersectionDelaySeconds))
                + " AS cost, -1::FLOAT8 AS reverse_cost FROM tb_road_link WHERE is_routable";
    }

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
     * @return 갈 수 있는 조합별 주행 시간. 갈 수 없는 조합은 빠진다. 회전 제한을 어겨야만 갈 수 있는 조합은
     *     {@link #TURN_RESTRICTION_PENALTY_SECONDS}가 더해져 있다
     */
    public List<DriveCost> findDriveCosts(List<SnappedPoint> starts, List<SnappedPoint> ends) {
        disableJit();
        Set<Long> restricted = restrictedLinkIds();
        List<Integer> batchEnds = new ArrayList<>();
        List<Integer> pairEnds = new ArrayList<>();
        for (int i = 0; i < ends.size(); i++) {
            (restricted.contains(ends.get(i).linkId()) ? pairEnds : batchEnds).add(i);
        }

        List<DriveCost> costs = new ArrayList<>();
        costs.addAll(queryCosts(BATCH_COST_SQL, starts, ends, batchEnds));
        costs.addAll(queryCosts(PAIR_COST_SQL, starts, ends, pairEnds));
        return costs;
    }

    // endIndexes에 해당하는 도착 후보만 넣어 계산하고, 결과의 도착 위치를 ends 기준으로 되돌린다.
    private List<DriveCost> queryCosts(
            String sql, List<SnappedPoint> starts, List<SnappedPoint> ends, List<Integer> endIndexes) {
        if (endIndexes.isEmpty()) {
            return List.of();
        }
        List<SnappedPoint> selected = endIndexes.stream().map(ends::get).toList();
        return jdbcTemplate.query(
                sql,
                Map.of(
                        "edgesSql", edgesSql,
                        "restrictionsSql", RESTRICTIONS_SQL,
                        "startLinkIds",
                                starts.stream().map(SnappedPoint::linkId).toList(),
                        "startFractions",
                                starts.stream().map(SnappedPoint::fraction).toList(),
                        "endLinkIds",
                                selected.stream().map(SnappedPoint::linkId).toList(),
                        "endFractions",
                                selected.stream().map(SnappedPoint::fraction).toList()),
                (rs, rowNum) -> new DriveCost(
                        rs.getInt("start_index"), endIndexes.get(rs.getInt("end_index")), rs.getDouble("agg_cost")));
    }

    // SET LOCAL은 트랜잭션이 끝나면 원래 설정으로 돌아가서 다른 쿼리에 영향을 주지 않는다
    private void disableJit() {
        jdbcTemplate.getJdbcOperations().execute(DISABLE_JIT_SQL);
    }

    private Set<Long> restrictedLinkIds() {
        Set<Long> ids = restrictedLinkIds;
        if (ids == null) {
            ids = Set.copyOf(jdbcTemplate.queryForList(RESTRICTED_LINKS_SQL, Map.of(), Long.class));
            restrictedLinkIds = ids;
        }
        return ids;
    }

    /**
     * 두 투영점 사이의 회전 제한을 지키는 최단 시간 차량 경로를 지나가는 링크 순서대로 반환한다. 회전 제한을 어기지 않고는 갈 수 없으면
     * 금지된 회전을 지나는 경로를 반환한다.
     *
     * @param start 출발 투영점 ({@link #snap} 결과)
     * @param end 도착 투영점 ({@link #snap} 결과)
     * @return 링크별 구간. 좌표는 WGS84(EPSG:4326), [경도, 위도] 순서. 경로가 없거나 두 투영점이 같으면 빈 목록
     */
    public List<PathPart> findPath(SnappedPoint start, SnappedPoint end) {
        disableJit();
        return jdbcTemplate.query(
                PATH_SQL,
                Map.of(
                        "edgesSql", edgesSql,
                        "restrictionsSql", RESTRICTIONS_SQL,
                        "pointsSql", pointsSql(List.of(start, end)),
                        "startFraction", start.fraction(),
                        "endFraction", end.fraction(),
                        "speedFactor", speedFactor,
                        "intersectionDelay", intersectionDelaySeconds),
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
     * @param seconds 최소 주행 시간(초). 회전 제한을 어겨야만 갈 수 있으면 {@link #TURN_RESTRICTION_PENALTY_SECONDS}가 더해져 있다
     */
    public record DriveCost(int startIndex, int endIndex, double seconds) {

        public boolean violatesTurnRestriction() {
            return seconds >= TURN_RESTRICTION_PENALTY_SECONDS;
        }
    }

    /**
     * @param distanceMeters 구간 거리(m)
     * @param durationSeconds 구간 주행 시간(초). 제한속도 기준
     * @param coordinates 구간 선형 좌표열. WGS84, [경도, 위도] 순서
     */
    public record PathPart(double distanceMeters, double durationSeconds, List<double[]> coordinates) {}
}
