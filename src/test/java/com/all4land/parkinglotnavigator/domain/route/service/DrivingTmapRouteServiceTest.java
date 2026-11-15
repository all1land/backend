package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.all4land.parkinglotnavigator.domain.route.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.domain.route.dto.DrivingRouteSegmentDTO;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnRouteErrCode;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 실제 적재 데이터(강서구 차량 네트워크, 강서구 주차장 seed)에 붙여서 검증한다. 데이터를 쓰지 않고 조회만 한다.
@SpringBootTest
class DrivingTmapRouteServiceTest {

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
        List<DrivingRouteSegmentDTO> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        assertConnects(segments, BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);
        // 발산역교차로의 공항대로36길 → 공항대로 좌회전이 금지라, 공항대로 위 지점(약 20m)에 투영해 출발한다.
        // 공항대로 → 방화대로 → 마곡서1로 주행 + 양 끝 접근 구간 약 20m, 33m
        assertThat(totalDistance(segments)).isBetween(2650, 2700);
    }

    @Test
    void doesNotArriveOnOppositeCarriagewayOfMultiLaneRoad() {
        // 주차장에서 가장 가까운 링크가 2차로 이상이면 그 도로의 반대 방향 차선에 도착하지 않는다.
        // 마지막 접근 구간이 가장 가까운 링크까지보다 반대 방향 차선 간격만큼 길어지지 않는지로 확인한다.
        List<Map<String, Object>> lots = jdbcTemplate.queryForList("""
                SELECT name, ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng
                FROM tb_parking_lot
                WHERE name <> ?
                ORDER BY id
                """, PARKING_LOT_FAR_FROM_ROAD);

        int multiLaneLots = 0;
        SoftAssertions softly = new SoftAssertions();
        for (Map<String, Object> lot : lots) {
            double lat = (double) lot.get("lat");
            double lng = (double) lot.get("lng");
            Map<String, Object> nearest = jdbcTemplate.queryForMap("""
                    SELECT l.lanes, ST_Distance(ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, l.geom::geography) AS m
                    FROM tb_road_link l
                    WHERE l.is_routable AND NOT l.is_ramp AND NOT l.is_intersection_internal
                      AND l.road_rank_code NOT IN ('101', '102') AND l.road_type_code NOT IN ('001', '002')
                    ORDER BY l.geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
                    LIMIT 1
                    """, lng, lat, lng, lat);
            Integer lanes = (Integer) nearest.get("lanes");
            if (lanes == null || lanes < 2) {
                continue;
            }
            multiLaneLots++;
            List<DrivingRouteSegmentDTO> segments = route(BALSAN_LAT, BALSAN_LNG, lat, lng);
            softly.assertThat((double) segments.getLast().distanceMeters())
                    .as("%s 도착 접근 구간", lot.get("name"))
                    .isLessThanOrEqualTo((double) nearest.get("m") + 5);
        }
        assertThat(multiLaneLots).isPositive();
        softly.assertAll();
    }

    @Test
    void avoidsProhibitedLeftTurn() {
        // 송정중교 교차로: 남부순환로 → 남부순환로11길 좌회전 금지 (진입 → 교차로 안 연결 링크 2개 → 진출)
        Map<String, Object> turn = jdbcTemplate.queryForMap("""
                SELECT r.from_link_id, r.to_link_id,
                       (SELECT sum(l.length_m) FROM unnest(r.link_path[2:cardinality(r.link_path) - 1]) c(id)
                        JOIN tb_road_link l ON l.id = c.id) AS connector_m,
                       f.length_m AS from_m, e.length_m AS to_m
                FROM tb_road_turn_restriction r
                JOIN tb_road_link f ON f.id = r.from_link_id
                JOIN tb_road_link e ON e.id = r.to_link_id
                WHERE r.from_link_id = 1150055900 AND r.to_link_id = 1150058600 AND r.turn_type_code = '101'
                """);
        double[] onFrom = pointOnLink((long) turn.get("from_link_id"), 0.5);
        double[] onTo = pointOnLink((long) turn.get("to_link_id"), 0.5);
        // 금지된 좌회전으로 바로 꺾을 때의 주행 거리
        double direct =
                (double) turn.get("from_m") * 0.5 + (double) turn.get("connector_m") + (double) turn.get("to_m") * 0.5;

        List<DrivingRouteSegmentDTO> segments = route(onFrom[1], onFrom[0], onTo[1], onTo[0]);

        assertConnects(segments, onFrom[1], onFrom[0], onTo[1], onTo[0]);
        assertThat((double) totalDistance(segments)).isGreaterThan(direct + 100);
    }

    @Test
    void doesNotSnapToIntersectionInternalLink() {
        // 교차로 안 연결 링크(양 끝 노드가 같은 교차로 그룹) 위의 점에서 출발해도 그 링크에 투영하지 않는다.
        // 교차로 한가운데서 출발하면 진입 링크를 거치지 않아 회전 제한을 피해 가게 된다.
        double[] inIntersection =
                jdbcTemplate.queryForObject("""
                SELECT ST_X(p), ST_Y(p)
                FROM (
                    SELECT ST_LineInterpolatePoint(geom, 0.5) AS p
                    FROM tb_road_link
                    WHERE is_routable AND left(start_node_id::TEXT, 8) = left(end_node_id::TEXT, 8) AND length_m >= 20
                    ORDER BY id
                    LIMIT 1
                ) x
                """, (rs, rowNum) -> new double[] {rs.getDouble(1), rs.getDouble(2)});

        List<DrivingRouteSegmentDTO> segments =
                route(inIntersection[1], inIntersection[0], SINBANGHWA_LAT, SINBANGHWA_LNG);

        DrivingRouteSegmentDTO access = segments.getFirst();
        assertThat(access.distanceMeters()).isPositive();
        // 투영점이 교차로 노드(링크 끝점)에 붙으면 연결 링크와도 거리가 0이라, 교차로 밖 링크가 투영점을 지나는지로 확인한다
        Boolean onOrdinaryLink = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM tb_road_link
                    WHERE is_routable
                      AND left(start_node_id::TEXT, 8) <> left(end_node_id::TEXT, 8)
                      AND ST_DWithin(geom::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, 0.01))
                """, Boolean.class, access.endLng(), access.endLat());
        assertThat(onOrdinaryLink).isTrue();
    }

    @Test
    void firstAndLastSegmentsAreWalkingAccessSegments() {
        List<DrivingRouteSegmentDTO> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        for (DrivingRouteSegmentDTO access : List.of(segments.getFirst(), segments.getLast())) {
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

        List<DrivingRouteSegmentDTO> segments = route(near[1], near[0], far[1], far[0]);

        // 첫 구간과 마지막 구간이 도보라는 것을 위치로 알 수 있게 거리 0이어도 넣는다
        assertThat(segments).hasSizeGreaterThanOrEqualTo(3);
        assertThat(segments.getFirst().distanceMeters()).isZero();
        assertThat(segments.getLast().distanceMeters()).isZero();
    }

    @Test
    void driveSegmentsMoveWithinSpeedLimits() {
        List<DrivingRouteSegmentDTO> segments = route(BALSAN_LAT, BALSAN_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        SoftAssertions softly = new SoftAssertions();
        for (DrivingRouteSegmentDTO segment : segments.subList(1, segments.size() - 1)) {
            double geometryLength = length(segment.coordinates());
            softly.assertThat((double) segment.distanceMeters())
                    .as("구간 거리 vs 좌표 길이")
                    .isCloseTo(geometryLength, within(Math.max(1.0, geometryLength * 0.02)));
            // 반올림 오차가 큰 짧은 구간은 속도를 보지 않는다. 강서구 범위의 최고 제한속도는 100km/h다
            if (segment.distanceMeters() >= 50) {
                double kph = segment.distanceMeters() / (double) segment.durationSeconds() * 3.6;
                softly.assertThat(kph)
                        .as("구간 속도(km/h) <= 최고 제한속도 × 속도 계수")
                        .isBetween(1.0, 100 * properties.speedFactor() * 1.1);
            }
        }
        softly.assertAll();
    }

    @Test
    void driveDurationAppliesSpeedFactor() {
        Map<String, Object> link = oneWayLink();
        long linkId = (long) link.get("id");
        double[] near = pointOnLink(linkId, 0.3);
        double[] far = pointOnLink(linkId, 0.7);
        Double travelTime = jdbcTemplate.queryForObject(
                "SELECT travel_time_s FROM tb_road_link WHERE id = ?", Double.class, linkId);

        List<DrivingRouteSegmentDTO> segments = route(near[1], near[0], far[1], far[0]);

        // 링크 위 0.3 → 0.7 구간만 달린다: 제한속도 주행 시간의 40% / 속도 계수
        assertThat((double) driveDuration(segments))
                .isCloseTo(travelTime * 0.4 / properties.speedFactor(), within(1.0));
    }

    @Test
    void addsIntersectionDelayWhenGoingStraightThroughIntersection() {
        // 같은 도로를 따라 교차로 하나를 직진: 진입 링크 → 교차로 안 연결 링크 1개 → 진출 링크
        Map<String, Object> straight = jdbcTemplate.queryForMap("""
                SELECT a.id AS a_id, b.id AS b_id,
                       a.travel_time_s AS a_t, c.travel_time_s AS c_t, b.travel_time_s AS b_t,
                       a.length_m AS a_m, c.length_m AS c_m, b.length_m AS b_m
                FROM tb_road_link a
                JOIN tb_road_link c ON c.start_node_id = a.end_node_id AND c.is_routable AND c.is_intersection_internal
                JOIN tb_road_link b ON b.start_node_id = c.end_node_id AND b.is_routable AND NOT b.is_intersection_internal
                WHERE a.is_routable AND NOT a.is_intersection_internal
                  AND NOT a.is_ramp AND NOT b.is_ramp
                  AND a.road_rank_code NOT IN ('101', '102') AND b.road_rank_code NOT IN ('101', '102')
                  AND a.road_type_code NOT IN ('001', '002') AND b.road_type_code NOT IN ('001', '002')
                  AND a.road_name = b.road_name AND a.length_m >= 100 AND b.length_m >= 100
                  AND NOT EXISTS (SELECT 1 FROM tb_road_turn_restriction r WHERE r.link_path = ARRAY[a.id, c.id, b.id])
                ORDER BY a.id
                LIMIT 1
                """);
        double[] onA = pointOnLink((long) straight.get("a_id"), 0.5);
        double[] onB = pointOnLink((long) straight.get("b_id"), 0.5);

        List<DrivingRouteSegmentDTO> segments = route(onA[1], onA[0], onB[1], onB[0]);

        double straightDistance =
                (double) straight.get("a_m") * 0.5 + (double) straight.get("c_m") + (double) straight.get("b_m") * 0.5;
        double straightTravelTime =
                (double) straight.get("a_t") * 0.5 + (double) straight.get("c_t") + (double) straight.get("b_t") * 0.5;
        assertThat((double) totalDistance(segments)).isCloseTo(straightDistance, within(2.0));
        // 보정 주행 시간 + 연결 링크 1개의 신호 대기 시간 (구간별 반올림 오차 허용)
        assertThat((double) driveDuration(segments))
                .isCloseTo(
                        straightTravelTime / properties.speedFactor() + properties.intersectionDelaySeconds(),
                        within(2.0));
    }

    @Test
    void followsOneWayDirection() {
        Map<String, Object> link = oneWayLink();
        long linkId = (long) link.get("id");
        double length = (double) link.get("length_m");
        double[] near = pointOnLink(linkId, 0.3);
        double[] far = pointOnLink(linkId, 0.7);

        List<DrivingRouteSegmentDTO> forward = route(near[1], near[0], far[1], far[0]);
        List<DrivingRouteSegmentDTO> backward = route(far[1], far[0], near[1], near[0]);

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

        List<DrivingRouteSegmentDTO> segments = route(onExpressway[1], onExpressway[0], SINBANGHWA_LAT, SINBANGHWA_LNG);

        DrivingRouteSegmentDTO access = segments.getFirst();
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
        List<DrivingRouteSegmentDTO> segments = route(BALSAN_LAT, BALSAN_LNG, BALSAN_LAT, BALSAN_LNG);

        assertThat(segments).isEmpty();
    }

    @Test
    void startFarFromRoadNetworkIsRejected() {
        assertErrorCode(
                () -> route(GANGNAM_LAT, GANGNAM_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG),
                EnRouteErrCode.ROAD_LINK_NOT_NEARBY);
    }

    @Test
    void endFarFromRoadNetworkIsRejected() {
        assertErrorCode(
                () -> route(BALSAN_LAT, BALSAN_LNG, GANGNAM_LAT, GANGNAM_LNG), EnRouteErrCode.ROAD_LINK_NOT_NEARBY);
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

        assertErrorCode(() -> route(BALSAN_LAT, BALSAN_LNG, lat, lng), EnRouteErrCode.ROAD_LINK_NOT_NEARBY);
        assertErrorCode(() -> route(lat, lng, BALSAN_LAT, BALSAN_LNG), EnRouteErrCode.ROAD_LINK_NOT_NEARBY);
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
                List<DrivingRouteSegmentDTO> segments =
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

    private List<DrivingRouteSegmentDTO> route(double startLat, double startLng, double endLat, double endLng) {
        return drivingRouteService.route(startLat, startLng, endLat, endLng);
    }

    // 첫 구간은 출발지에서 시작, 마지막 구간은 도착지에서 끝, 구간끼리 끊김 없음, 각 구간 coordinates 양 끝 = 시작/끝 필드
    private static void assertConnects(
            List<DrivingRouteSegmentDTO> segments, double startLat, double startLng, double endLat, double endLng) {
        assertThat(segments).isNotEmpty();
        assertThat(segments.getFirst().startLat()).isCloseTo(startLat, within(1e-9));
        assertThat(segments.getFirst().startLng()).isCloseTo(startLng, within(1e-9));
        assertThat(segments.getLast().endLat()).isCloseTo(endLat, within(1e-9));
        assertThat(segments.getLast().endLng()).isCloseTo(endLng, within(1e-9));

        for (int i = 0; i + 1 < segments.size(); i++) {
            DrivingRouteSegmentDTO current = segments.get(i);
            DrivingRouteSegmentDTO next = segments.get(i + 1);
            assertThat(next.startLat()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLat(), within(1e-9));
            assertThat(next.startLng()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLng(), within(1e-9));
        }

        for (DrivingRouteSegmentDTO segment : segments) {
            assertThat(segment.coordinates()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(segment.coordinates().getFirst()).containsExactly(segment.startLng(), segment.startLat());
            assertThat(segment.coordinates().getLast()).containsExactly(segment.endLng(), segment.endLat());
        }
    }

    private static void assertErrorCode(Runnable call, EnRouteErrCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(expected));
    }

    // 양 끝 접근 구간을 뺀 주행 구간 소요 시간 합(초)
    private static int driveDuration(List<DrivingRouteSegmentDTO> segments) {
        return segments.subList(1, segments.size() - 1).stream()
                .mapToInt(DrivingRouteSegmentDTO::durationSeconds)
                .sum();
    }

    private static int totalDistance(List<DrivingRouteSegmentDTO> segments) {
        return segments.stream()
                .mapToInt(DrivingRouteSegmentDTO::distanceMeters)
                .sum();
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
