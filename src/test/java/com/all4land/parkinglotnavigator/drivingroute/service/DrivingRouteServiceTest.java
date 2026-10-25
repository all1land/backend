package com.all4land.parkinglotnavigator.drivingroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.all4land.parkinglotnavigator.drivingroute.DrivingRouteErrorCode;
import com.all4land.parkinglotnavigator.drivingroute.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.drivingroute.dto.DrivingRouteSegment;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 실제 적재 데이터(강서구 차량 네트워크, 강서구 주차장 seed)에 붙여서 검증한다. 데이터를 쓰지 않고 조회만 한다.
@SpringBootTest
class DrivingRouteServiceTest {

    // 발산역 사거리 부근
    private static final double BALSAN_LAT = 37.5585;
    private static final double BALSAN_LNG = 126.8376;

    // 강서구 주차장 seed: 신방화역
    private static final double SINBANGHWA_LAT = 37.5667867;
    private static final double SINBANGHWA_LNG = 126.8177876;

    // 강남역: 강서구 차량 네트워크 밖
    private static final double GANGNAM_LAT = 37.4979;
    private static final double GANGNAM_LNG = 127.0276;

    // 표준노드링크는 시·군·구도 중 왕복 2차로 이상만 구축해 주택가 골목이 빠져 있다.
    // 이 주차장은 가장 가까운 차량 도로(강서로)까지 약 200.5m라 투영 허용 거리(200m)를 넘는다. 골목길 처리 방안은 추후 검토.
    private static final String PARKING_LOT_FAR_FROM_ROAD = "화곡5-2";

    @Autowired
    private DrivingRouteService drivingRouteService;

    @Autowired
    private DrivingRouteProperties properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void routeStartsAtOriginEndsAtDestinationAndIsContinuous() {
        List<DrivingRouteSegment> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        assertConnects(segments, BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);
        // 공항대로 → 방화대로 → 마곡서1로 주행 약 2,633m + 양 끝 접근 구간 약 7m, 33m
        assertThat(totalDistance(segments)).isBetween(2650, 2700);
    }

    @Test
    void firstAndLastSegmentsAreWalkingAccessSegments() {
        List<DrivingRouteSegment> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        for (DrivingRouteSegment access : List.of(segments.getFirst(), segments.getLast())) {
            assertThat(access.coordinates()).hasSize(2);
            assertThat((double) access.durationSeconds())
                    .as("접근 구간 소요 시간 = 거리 / 보행 속도")
                    .isCloseTo(access.distanceMeters() / properties.accessWalkingSpeedMps(), within(1.0));
        }
    }

    @Test
    void accessSegmentsExistEvenWhenPointsAreOnRoad() {
        long linkId = (long) oneWayLink().get("id");
        double[] near = pointOnLink(linkId, 0.3);
        double[] far = pointOnLink(linkId, 0.7);

        List<DrivingRouteSegment> segments = route(near[1], near[0], far[1], far[0]);

        // 첫 구간과 마지막 구간이 도보라는 것을 위치로 알 수 있게 거리 0이어도 넣는다
        assertThat(segments).hasSizeGreaterThanOrEqualTo(3);
        assertThat(segments.getFirst().distanceMeters()).isZero();
        assertThat(segments.getLast().distanceMeters()).isZero();
    }

    @Test
    void driveSegmentsMoveWithinSpeedLimits() {
        List<DrivingRouteSegment> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        SoftAssertions softly = new SoftAssertions();
        for (DrivingRouteSegment segment : segments.subList(1, segments.size() - 1)) {
            double geometryLength = length(segment.coordinates());
            softly.assertThat((double) segment.distanceMeters())
                    .as("구간 거리 vs 좌표 길이")
                    .isCloseTo(geometryLength, within(Math.max(1.0, geometryLength * 0.02)));
            // 반올림 오차가 큰 짧은 구간은 속도를 보지 않는다
            if (segment.distanceMeters() >= 50) {
                double kph = segment.distanceMeters() / (double) segment.durationSeconds() * 3.6;
                softly.assertThat(kph).as("구간 속도(km/h)").isBetween(9.0, 110.0);
            }
        }
        softly.assertAll();
    }

    @Test
    void followsOneWayDirection() {
        Map<String, Object> link = oneWayLink();
        long linkId = (long) link.get("id");
        double length = (double) link.get("length_m");
        double[] near = pointOnLink(linkId, 0.3);
        double[] far = pointOnLink(linkId, 0.7);

        List<DrivingRouteSegment> forward = route(near[1], near[0], far[1], far[0]);
        List<DrivingRouteSegment> backward = route(far[1], far[0], near[1], near[0]);

        assertConnects(forward, near[1], near[0], far[1], far[0]);
        assertConnects(backward, far[1], far[0], near[1], near[0]);
        // 진행 방향으로는 링크 위 0.3 → 0.7 구간만 달린다
        assertThat(totalDistance(forward)).isCloseTo((int) Math.round(length * 0.4), within(2));
        // 일방통행이라 반대로는 바로 갈 수 없어 다른 길로 돌아간다
        assertThat(totalDistance(backward)).isGreaterThan(totalDistance(forward) * 2);
    }

    @Test
    void doesNotSnapToExpressway() {
        double[] onExpressway = pointOnExpresswayNearOrdinaryRoad();

        List<DrivingRouteSegment> segments = route(onExpressway[1], onExpressway[0], SINBANGHWA_LAT, SINBANGHWA_LNG);

        DrivingRouteSegment access = segments.getFirst();
        assertThat(access.distanceMeters()).isPositive();
        String roadRank = jdbcTemplate.queryForObject("""
                SELECT road_rank_code
                FROM tb_road_link
                WHERE is_routable
                ORDER BY geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
                LIMIT 1
                """, String.class, access.endLng(), access.endLat());
        assertThat(roadRank).isNotIn("101", "102");
    }

    @Test
    void samePointReturnsEmptyRoute() {
        List<DrivingRouteSegment> segments = route(BALSAN_LAT, BALSAN_LNG, BALSAN_LAT, BALSAN_LNG);

        assertThat(segments).isEmpty();
    }

    @Test
    void startFarFromRoadNetworkIsRejected() {
        assertErrorCode(
                () -> route(GANGNAM_LAT, GANGNAM_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG),
                DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY);
    }

    @Test
    void endFarFromRoadNetworkIsRejected() {
        assertErrorCode(
                () -> route(BALSAN_LAT, BALSAN_LNG, GANGNAM_LAT, GANGNAM_LNG),
                DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY);
    }

    @Test
    void parkingLotFarFromRoadNetworkIsRejected() {
        Map<String, Object> lot = jdbcTemplate.queryForMap("""
                SELECT ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng
                FROM tb_parking_lot
                WHERE name = ?
                """, PARKING_LOT_FAR_FROM_ROAD);
        double lat = (double) lot.get("lat");
        double lng = (double) lot.get("lng");

        assertErrorCode(() -> route(BALSAN_LAT, BALSAN_LNG, lat, lng), DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY);
        assertErrorCode(() -> route(lat, lng, BALSAN_LAT, BALSAN_LNG), DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY);
    }

    @Test
    void routesBetweenBalsanAndEveryOtherParkingLot() {
        List<Map<String, Object>> lots = jdbcTemplate.queryForList("""
                SELECT name, ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng
                FROM tb_parking_lot
                WHERE name <> ?
                ORDER BY id
                """, PARKING_LOT_FAR_FROM_ROAD);
        assertThat(lots).hasSize(36);

        SoftAssertions softly = new SoftAssertions();
        for (Map<String, Object> lot : lots) {
            double lat = (double) lot.get("lat");
            double lng = (double) lot.get("lng");
            double straight = haversine(BALSAN_LNG, BALSAN_LAT, lng, lat);
            for (boolean outbound : new boolean[] {true, false}) {
                List<DrivingRouteSegment> segments =
                        outbound ? route(BALSAN_LAT, BALSAN_LNG, lat, lng) : route(lat, lng, BALSAN_LAT, BALSAN_LNG);
                String label = lot.get("name") + (outbound ? " 가는 길" : " 오는 길");
                softly.assertThat(segments).as(label).hasSizeGreaterThanOrEqualTo(2);
                softly.assertThat((double) totalDistance(segments))
                        .as(label + " 총거리 >= 직선거리")
                        .isGreaterThanOrEqualTo(straight - segments.size());
            }
        }
        softly.assertAll();
    }

    private List<DrivingRouteSegment> route(double startLat, double startLng, double endLat, double endLng) {
        return drivingRouteService.route(startLat, startLng, endLat, endLng);
    }

    // 첫 구간은 출발지에서 시작, 마지막 구간은 도착지에서 끝, 구간끼리 끊김 없음, 각 구간 coordinates 양 끝 = 시작/끝 필드
    private static void assertConnects(
            List<DrivingRouteSegment> segments, double startLat, double startLng, double endLat, double endLng) {
        assertThat(segments).isNotEmpty();
        assertThat(segments.getFirst().startLat()).isCloseTo(startLat, within(1e-9));
        assertThat(segments.getFirst().startLng()).isCloseTo(startLng, within(1e-9));
        assertThat(segments.getLast().endLat()).isCloseTo(endLat, within(1e-9));
        assertThat(segments.getLast().endLng()).isCloseTo(endLng, within(1e-9));

        for (int i = 0; i + 1 < segments.size(); i++) {
            DrivingRouteSegment current = segments.get(i);
            DrivingRouteSegment next = segments.get(i + 1);
            assertThat(next.startLat()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLat(), within(1e-9));
            assertThat(next.startLng()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLng(), within(1e-9));
        }

        for (DrivingRouteSegment segment : segments) {
            assertThat(segment.coordinates()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(segment.coordinates().getFirst()).containsExactly(segment.startLng(), segment.startLat());
            assertThat(segment.coordinates().getLast()).containsExactly(segment.endLng(), segment.endLat());
        }
    }

    private static void assertErrorCode(Runnable call, DrivingRouteErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(expected));
    }

    private static int totalDistance(List<DrivingRouteSegment> segments) {
        return segments.stream().mapToInt(DrivingRouteSegment::distanceMeters).sum();
    }

    // 일방통행 도로의 진행 방향 링크 하나. 구축 기준상 일방통행 도로는 반대 방향 링크가 미사용(ROAD_USE=1)으로 약 12m 옆에 있다.
    private Map<String, Object> oneWayLink() {
        return jdbcTemplate.queryForMap("""
                SELECT l.id, l.length_m
                FROM tb_road_link l
                JOIN tb_road_link u
                  ON NOT u.is_in_use
                 AND ST_DWithin(ST_StartPoint(u.geom)::geography, ST_EndPoint(l.geom)::geography, 25)
                 AND ST_DWithin(ST_EndPoint(u.geom)::geography, ST_StartPoint(l.geom)::geography, 25)
                WHERE l.is_routable
                  AND NOT l.is_ramp
                  AND l.road_rank_code NOT IN ('101', '102')
                  AND l.road_type_code NOT IN ('001', '002')
                  AND l.length_m >= 150
                ORDER BY l.length_m DESC, l.id
                LIMIT 1
                """);
    }

    // 고속·도시고속국도 링크의 중간 지점 중 150m 안에 일반 도로가 있는 곳의 [경도, 위도]
    private double[] pointOnExpresswayNearOrdinaryRoad() {
        return jdbcTemplate.queryForObject("""
                SELECT ST_X(p.pt), ST_Y(p.pt)
                FROM tb_road_link e
                CROSS JOIN LATERAL (SELECT ST_LineInterpolatePoint(e.geom, 0.5) AS pt) p
                WHERE e.is_routable
                  AND e.road_rank_code IN ('101', '102')
                  AND e.length_m >= 200
                  AND EXISTS (
                      SELECT 1
                      FROM tb_road_link o
                      WHERE o.is_routable
                        AND NOT o.is_ramp
                        AND o.road_rank_code NOT IN ('101', '102')
                        AND o.road_type_code NOT IN ('001', '002')
                        AND ST_DWithin(o.geom::geography, p.pt::geography, 150))
                ORDER BY e.id
                LIMIT 1
                """, (rs, rowNum) -> new double[] {rs.getDouble(1), rs.getDouble(2)});
    }

    // 링크 위 fraction 지점의 [경도, 위도]
    private double[] pointOnLink(long linkId, double fraction) {
        return jdbcTemplate.queryForObject(
                """
                SELECT ST_X(p), ST_Y(p)
                FROM (SELECT ST_LineInterpolatePoint(geom, ?) AS p FROM tb_road_link WHERE id = ?) x
                """, (rs, rowNum) -> new double[] {rs.getDouble(1), rs.getDouble(2)}, fraction, linkId);
    }

    private static double length(List<double[]> coordinates) {
        double sum = 0;
        for (int i = 0; i + 1 < coordinates.size(); i++) {
            double[] a = coordinates.get(i);
            double[] b = coordinates.get(i + 1);
            sum += haversine(a[0], a[1], b[0], b[1]);
        }
        return sum;
    }

    private static double haversine(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * 6_371_008.8 * Math.asin(Math.sqrt(h));
    }
}
