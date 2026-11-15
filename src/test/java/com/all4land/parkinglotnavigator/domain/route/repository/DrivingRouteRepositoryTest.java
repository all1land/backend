package com.all4land.parkinglotnavigator.domain.route.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.DriveCost;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.SnappedPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 실제 적재 데이터(강서구 차량 네트워크, 강서구 주차장 seed)에 붙여서 검증한다. 데이터를 쓰지 않고 조회만 한다.
@SpringBootTest
class DrivingRouteRepositoryTest {

    private static final int CANDIDATES = 4;
    private static final double MAX_SNAP_METERS = 200;

    @Autowired
    private DrivingRouteRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void batchDriveCostsMatchPairwiseDriveCosts() {
        List<SnappedPoint> ends = parkingLotCandidates();
        // 금지 경로(회전 금지, U턴 금지) 링크 위의 도착 후보도 있어야 두 계산 방식을 모두 거친다
        assertThat(ends).anyMatch(this::onProhibitedPath);
        assertThat(ends).anyMatch(end -> !onProhibitedPath(end));

        SoftAssertions softly = new SoftAssertions();
        // 발산역교차로·송정중교 좌회전 금지의 진입 링크 위에서 출발하는 경우를 포함한다
        for (double[] origin : List.of(pointOnLink(1150435502L, 0.9), pointOnLink(1150055900L, 0.5))) {
            List<SnappedPoint> starts = repository.snap(origin[1], origin[0], CANDIDATES, MAX_SNAP_METERS);

            Map<String, Double> together = byPair(repository.findDriveCosts(starts, ends));
            Map<String, Double> oneByOne = new HashMap<>();
            for (int e = 0; e < ends.size(); e++) {
                for (DriveCost cost : repository.findDriveCosts(starts, List.of(ends.get(e)))) {
                    oneByOne.put(cost.startIndex() + ":" + e, cost.seconds());
                }
            }

            softly.assertThat(together)
                    .as("출발 %s", List.of(origin[0], origin[1]))
                    .hasSameSizeAs(oneByOne);
            oneByOne.forEach((pair, seconds) -> softly.assertThat(together.get(pair))
                    .as("출발 후보:도착 후보 %s", pair)
                    .isCloseTo(seconds, org.assertj.core.api.Assertions.within(1e-6)));
        }
        softly.assertAll();
    }

    @Test
    void pathDoesNotUTurnOutsideAllowedSpots() {
        // U턴 허용 지점이 아닌 교차로: 진입 링크 → 교차로 안 연결 링크 3개 → 반대 방향 진출 링크.
        // 데이터를 자른 범위의 경계 근처는 U턴을 막으면 차를 돌려 돌아올 길이 없을 수 있어 강서구(링크 ID 1150) 안에서 고른다.
        Map<String, Object> uturn = jdbcTemplate.queryForMap("""
                SELECT u.from_link_id, u.to_link_id,
                       (SELECT sum(l.length_m) FROM unnest(u.link_path[2:cardinality(u.link_path) - 1]) c(id)
                        JOIN tb_road_link l ON l.id = c.id) AS connector_m,
                       f.length_m AS from_m, t.length_m AS to_m
                FROM tb_road_uturn_prohibition u
                JOIN tb_road_link f ON f.id = u.from_link_id
                JOIN tb_road_link t ON t.id = u.to_link_id
                WHERE cardinality(u.link_path) = 5 AND f.length_m >= 100 AND t.length_m >= 100
                  AND u.from_link_id::TEXT LIKE '1150%'
                ORDER BY u.from_link_id, u.to_link_id
                LIMIT 1
                """);
        // 후보 선택을 거치지 않도록 진입 링크 30% 지점과 진출 링크 30% 지점을 직접 지정한다
        SnappedPoint onFrom = new SnappedPoint((long) uturn.get("from_link_id"), 0.3, 0, 0, 0);
        SnappedPoint onTo = new SnappedPoint((long) uturn.get("to_link_id"), 0.3, 0, 0, 0);
        // 금지된 U턴으로 바로 돌아 나올 때의 주행 거리
        double direct = (double) uturn.get("from_m") * 0.7
                + (double) uturn.get("connector_m")
                + (double) uturn.get("to_m") * 0.3;

        // U턴하지 않고 돌아오는 합법적인 경로가 있다
        assertThat(repository.findDriveCosts(List.of(onFrom), List.of(onTo)))
                .singleElement()
                .satisfies(cost -> assertThat(cost.violatesTurnRestriction()).isFalse());
        double distance = repository.findPath(onFrom, onTo).stream()
                .mapToDouble(DrivingRouteRepository.PathPart::distanceMeters)
                .sum();

        assertThat(distance).isGreaterThan(direct + 50);
    }

    // 주차장마다 도착 후보(투영점) 최대 4개
    private List<SnappedPoint> parkingLotCandidates() {
        List<SnappedPoint> ends = new ArrayList<>();
        jdbcTemplate
                .query(
                        "SELECT ST_Y(location::geometry), ST_X(location::geometry) FROM tb_parking_lot ORDER BY id",
                        (rs, rowNum) -> new double[] {rs.getDouble(1), rs.getDouble(2)})
                .forEach(lot -> ends.addAll(repository.snap(lot[0], lot[1], CANDIDATES, MAX_SNAP_METERS)));
        return ends;
    }

    private boolean onProhibitedPath(SnappedPoint point) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM tb_road_turn_restriction
                    WHERE link_path IS NOT NULL AND turn_type_code IN ('101', '102', '103', '003', '002')
                      AND ? = ANY(link_path)
                    UNION ALL
                    SELECT 1 FROM tb_road_uturn_prohibition WHERE ? = ANY(link_path))
                """, Boolean.class, point.linkId(), point.linkId()));
    }

    private static Map<String, Double> byPair(List<DriveCost> costs) {
        Map<String, Double> map = new HashMap<>();
        costs.forEach(cost -> map.put(cost.startIndex() + ":" + cost.endIndex(), cost.seconds()));
        return map;
    }

    // 링크 위 fraction 지점의 [경도, 위도]
    private double[] pointOnLink(long linkId, double fraction) {
        return jdbcTemplate.queryForObject(
                """
                SELECT ST_X(p), ST_Y(p)
                FROM (SELECT ST_LineInterpolatePoint(geom, ?) AS p FROM tb_road_link WHERE id = ?) x
                """, (rs, rowNum) -> new double[] {rs.getDouble(1), rs.getDouble(2)}, fraction, linkId);
    }
}
