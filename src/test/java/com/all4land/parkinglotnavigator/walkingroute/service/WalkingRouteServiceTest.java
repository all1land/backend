package com.all4land.parkinglotnavigator.walkingroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteErrorCode;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteProperties;
import com.all4land.parkinglotnavigator.walkingroute.dto.WalkingRouteSegment;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 실제 적재 데이터(강서구 도보 네트워크, 강서구 주차장 seed)에 붙여서 검증한다. 데이터를 쓰지 않고 조회만 한다.
@SpringBootTest
class WalkingRouteServiceTest {

    // 강서구 주차장 seed: 신방화역, 개화역
    private static final double SINBANGHWA_LAT = 37.5667867;
    private static final double SINBANGHWA_LNG = 126.8177876;
    private static final double GAEHWA_LAT = 37.5778861;
    private static final double GAEHWA_LNG = 126.798212;

    // 강남역: 강서구 도보 네트워크 밖
    private static final double GANGNAM_LAT = 37.4979;
    private static final double GANGNAM_LNG = 127.0276;

    // 가장 큰 연결 요소와 이어지지 않는 작은 연결 요소의 링크
    private static final long ISOLATED_LINK_ID = 221942;

    @Autowired
    private WalkingRouteService walkingRouteService;

    @Autowired
    private WalkingRouteProperties properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void routeStartsAtOriginEndsAtDestinationAndIsContinuous() {
        List<WalkingRouteSegment> segments = route(SINBANGHWA_LAT, SINBANGHWA_LNG, GAEHWA_LAT, GAEHWA_LNG);

        assertConnects(segments, SINBANGHWA_LAT, SINBANGHWA_LNG, GAEHWA_LAT, GAEHWA_LNG);
        // 네트워크 경로 2,731.4m + 양 끝 접근 구간 약 18m, 37m
        assertThat(totalDistance(segments)).isBetween(2770, 2800);
    }

    @Test
    void reverseRouteHasSameDistance() {
        int forward = totalDistance(route(SINBANGHWA_LAT, SINBANGHWA_LNG, GAEHWA_LAT, GAEHWA_LNG));
        List<WalkingRouteSegment> reverse = route(GAEHWA_LAT, GAEHWA_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        assertConnects(reverse, GAEHWA_LAT, GAEHWA_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);
        // 구간별 반올림 오차만 허용
        assertThat(totalDistance(reverse)).isCloseTo(forward, within(reverse.size()));
    }

    @Test
    void segmentDistanceMatchesGeometryAndDurationMatchesSpeed() {
        List<WalkingRouteSegment> segments = route(SINBANGHWA_LAT, SINBANGHWA_LNG, GAEHWA_LAT, GAEHWA_LNG);

        SoftAssertions softly = new SoftAssertions();
        for (WalkingRouteSegment segment : segments) {
            double geometryLength = length(segment.coordinates());
            softly.assertThat((double) segment.distanceMeters())
                    .as("구간 거리 vs 좌표 길이")
                    .isCloseTo(geometryLength, within(Math.max(1.0, geometryLength * 0.02)));
            softly.assertThat((double) segment.durationSeconds())
                    .as("소요 시간 = 거리 / 보행 속도")
                    .isCloseTo(segment.distanceMeters() / properties.walkingSpeedMps(), within(1.0));
        }
        softly.assertAll();
    }

    @Test
    void routeWithinSameLinkUsesOnlyThePartBetweenPoints() {
        Map<String, Object> link = longLinkInMainComponent();
        double length = (double) link.get("length_m");
        double[] near = pointOnLink((long) link.get("id"), 0.3);
        double[] far = pointOnLink((long) link.get("id"), 0.7);

        List<WalkingRouteSegment> segments = route(near[1], near[0], far[1], far[0]);

        assertConnects(segments, near[1], near[0], far[1], far[0]);
        assertThat(totalDistance(segments)).isCloseTo((int) Math.round(length * 0.4), within(2));
    }

    @Test
    void routeAgainstLinkDirectionReversesCoordinates() {
        Map<String, Object> link = longLinkInMainComponent();
        double length = (double) link.get("length_m");
        double[] near = pointOnLink((long) link.get("id"), 0.3);
        double[] far = pointOnLink((long) link.get("id"), 0.7);

        // 링크 저장 방향(0 → 1)과 반대로 0.7 지점에서 0.3 지점으로 걷는다.
        List<WalkingRouteSegment> segments = route(far[1], far[0], near[1], near[0]);

        assertConnects(segments, far[1], far[0], near[1], near[0]);
        assertThat(totalDistance(segments)).isCloseTo((int) Math.round(length * 0.4), within(2));
    }

    @Test
    void samePointReturnsEmptyRoute() {
        List<WalkingRouteSegment> segments = route(SINBANGHWA_LAT, SINBANGHWA_LNG, SINBANGHWA_LAT, SINBANGHWA_LNG);

        assertThat(segments).isEmpty();
    }

    @Test
    void startFarFromWalkNetworkIsRejected() {
        assertErrorCode(
                () -> route(GANGNAM_LAT, GANGNAM_LNG, GAEHWA_LAT, GAEHWA_LNG),
                WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY);
    }

    @Test
    void endFarFromWalkNetworkIsRejected() {
        assertErrorCode(
                () -> route(SINBANGHWA_LAT, SINBANGHWA_LNG, GANGNAM_LAT, GANGNAM_LNG),
                WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY);
    }

    @Test
    void unreachableComponentIsRejected() {
        double[] isolated = pointOnLink(ISOLATED_LINK_ID, 0.5);

        assertErrorCode(
                () -> route(SINBANGHWA_LAT, SINBANGHWA_LNG, isolated[1], isolated[0]),
                WalkingRouteErrorCode.WALKING_ROUTE_NOT_FOUND);
    }

    @Test
    void routesBetweenSinbanghwaAndEveryParkingLot() {
        List<Map<String, Object>> lots = jdbcTemplate.queryForList("""
                SELECT name, ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng
                FROM tb_parking_lot
                WHERE name <> '신방화역'
                ORDER BY id
                """);
        assertThat(lots).hasSize(36);

        SoftAssertions softly = new SoftAssertions();
        for (Map<String, Object> lot : lots) {
            double lat = (double) lot.get("lat");
            double lng = (double) lot.get("lng");
            double straight = haversine(SINBANGHWA_LNG, SINBANGHWA_LAT, lng, lat);
            for (boolean outbound : new boolean[] {true, false}) {
                List<WalkingRouteSegment> segments = outbound
                        ? route(SINBANGHWA_LAT, SINBANGHWA_LNG, lat, lng)
                        : route(lat, lng, SINBANGHWA_LAT, SINBANGHWA_LNG);
                String label = lot.get("name") + (outbound ? " 가는 길" : " 오는 길");
                softly.assertThat(segments).as(label).isNotEmpty();
                softly.assertThat((double) totalDistance(segments))
                        .as(label + " 총거리 >= 직선거리")
                        .isGreaterThanOrEqualTo(straight - segments.size());
            }
        }
        softly.assertAll();
    }

    private List<WalkingRouteSegment> route(double startLat, double startLng, double endLat, double endLng) {
        return walkingRouteService.route(startLat, startLng, endLat, endLng);
    }

    // 첫 구간은 출발지에서 시작, 마지막 구간은 도착지에서 끝, 구간끼리 끊김 없음, 각 구간 coordinates 양 끝 = 시작/끝 필드
    private static void assertConnects(
            List<WalkingRouteSegment> segments, double startLat, double startLng, double endLat, double endLng) {
        assertThat(segments).isNotEmpty();
        assertThat(segments.getFirst().startLat()).isCloseTo(startLat, within(1e-9));
        assertThat(segments.getFirst().startLng()).isCloseTo(startLng, within(1e-9));
        assertThat(segments.getLast().endLat()).isCloseTo(endLat, within(1e-9));
        assertThat(segments.getLast().endLng()).isCloseTo(endLng, within(1e-9));

        for (int i = 0; i + 1 < segments.size(); i++) {
            WalkingRouteSegment current = segments.get(i);
            WalkingRouteSegment next = segments.get(i + 1);
            assertThat(next.startLat()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLat(), within(1e-9));
            assertThat(next.startLng()).as("구간 %d → %d 이어짐", i, i + 1).isCloseTo(current.endLng(), within(1e-9));
        }

        for (WalkingRouteSegment segment : segments) {
            assertThat(segment.coordinates()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(segment.coordinates().getFirst()).containsExactly(segment.startLng(), segment.startLat());
            assertThat(segment.coordinates().getLast()).containsExactly(segment.endLng(), segment.endLat());
        }
    }

    private static void assertErrorCode(Runnable call, WalkingRouteErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(expected));
    }

    private static int totalDistance(List<WalkingRouteSegment> segments) {
        return segments.stream().mapToInt(WalkingRouteSegment::distanceMeters).sum();
    }

    // 가장 큰 연결 요소에 속한 200m 이상 직선에 가까운 보행 링크 하나
    private Map<String, Object> longLinkInMainComponent() {
        return jdbcTemplate.queryForMap("""
                WITH cc AS (
                    SELECT node, component
                    FROM pgr_connectedComponents(
                        'SELECT id, start_node_id AS source, end_node_id AS target, length_m AS cost FROM tb_walk_link')
                ), main AS (
                    SELECT component FROM cc GROUP BY component ORDER BY count(*) DESC LIMIT 1
                )
                SELECT l.id, l.length_m
                FROM tb_walk_link l
                JOIN cc ON cc.node = l.start_node_id
                WHERE cc.component = (SELECT component FROM main)
                  AND LEFT(l.link_type_code, 1) = '1'
                  AND l.start_node_id <> l.end_node_id
                  AND l.length_m >= 200
                  AND ST_NPoints(l.geom) = 2
                ORDER BY l.id
                LIMIT 1
                """);
    }

    // 링크 위 fraction 지점의 [경도, 위도]
    private double[] pointOnLink(long linkId, double fraction) {
        return jdbcTemplate.queryForObject(
                """
                SELECT ST_X(p), ST_Y(p)
                FROM (SELECT ST_LineInterpolatePoint(geom, ?) AS p FROM tb_walk_link WHERE id = ?) x
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
