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
        // 회전 제한 경로 링크 위의 도착 후보도 있어야 두 계산 방식을 모두 거친다
        assertThat(ends).anyMatch(this::onRestrictionPath);
        assertThat(ends).anyMatch(end -> !onRestrictionPath(end));

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

    private boolean onRestrictionPath(SnappedPoint point) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM tb_road_turn_restriction
                    WHERE link_path IS NOT NULL AND turn_type_code IN ('101', '102', '103', '003', '002')
                      AND ? = ANY(link_path))
                """, Boolean.class, point.linkId()));
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
