package com.all4land.parkinglotnavigator.integratedroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.integratedroute.dto.TravelMode;
import com.all4land.parkinglotnavigator.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 실제 주차장·도보·차량 데이터를 조회한다. 데이터를 추가하거나 변경하지 않는다.
@SpringBootTest
class IntegratedRouteServiceTest {

    @Autowired
    private IntegratedRouteService integratedRouteService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void combinesRealWalkingAndDrivingRoutes() {
        Long parkingId = sinbanghwaParkingId();

        // 출발 측 POI 자동 조회를 대신하는 테스트용 좌표이다.
        double originLat = 37.5586;
        double originLng = 126.8377;
        double departurePoiLat = 37.5585;
        double departurePoiLng = 126.8376;
        double destinationLat = 37.5673;
        double destinationLng = 126.8270;

        var response = integratedRouteService.route(
                originLat, originLng, departurePoiLat, departurePoiLng, parkingId, destinationLat, destinationLng);

        assertThat(response.segments()).isNotEmpty();
        assertThat(response.segments()).anyMatch(segment -> segment.mode() == TravelMode.WALKING);
        assertThat(response.segments()).anyMatch(segment -> segment.mode() == TravelMode.DRIVING);

        double[] first = response.segments().getFirst().coordinates().getFirst();
        double[] last = response.segments().getLast().coordinates().getLast();

        assertThat(first[0]).isCloseTo(originLng, within(1e-9));
        assertThat(first[1]).isCloseTo(originLat, within(1e-9));
        assertThat(last[0]).isCloseTo(destinationLng, within(1e-9));
        assertThat(last[1]).isCloseTo(destinationLat, within(1e-9));

        long distanceSum = response.segments().stream()
                .mapToLong(segment -> segment.distanceMeters())
                .sum();

        long durationSum = response.segments().stream()
                .mapToLong(segment -> segment.durationSeconds())
                .sum();

        assertThat(response.totalDistanceMeters()).isEqualTo(distanceSum).isPositive();
        assertThat(response.totalDurationSeconds()).isEqualTo(durationSum).isPositive();

        for (int index = 0; index < response.segments().size(); index++) {
            assertThat(response.segments().get(index).sequence()).isEqualTo(index + 1);

            if (index > 0) {
                double[] previous =
                        response.segments().get(index - 1).coordinates().getLast();
                double[] current = response.segments().get(index).coordinates().getFirst();

                assertThat(current[0]).isCloseTo(previous[0], within(1e-9));
                assertThat(current[1]).isCloseTo(previous[1], within(1e-9));
            }
        }
    }

    @Test
    void samePointForAllStagesReturnsEmptyRoute() {
        Long parkingId = sinbanghwaParkingId();
        var parking = parkingLotService.getParkingLot(parkingId);

        var response = integratedRouteService.route(
                parking.latitude(),
                parking.longitude(),
                parking.latitude(),
                parking.longitude(),
                parkingId,
                parking.latitude(),
                parking.longitude());

        assertThat(response.segments()).isEmpty();
        assertThat(response.totalDistanceMeters()).isZero();
        assertThat(response.totalDurationSeconds()).isZero();
    }

    @Test
    void lastWalkingFailureIsPropagatedInsteadOfReturningPartialRoute() {
        Long parkingId = sinbanghwaParkingId();

        // 앞부분 계산 후, 강서구 보행망 밖인 강남역을 목적지로 지정한다.
        assertThatThrownBy(() -> integratedRouteService.route(
                        37.5585, 126.8376, 37.5585, 126.8376, parkingId, 37.4979, 127.0276))
                .isInstanceOfSatisfying(BusinessException.class, exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY));
    }

    @Test
    void rejectsInvalidCoordinateBeforeCalculatingRoute() {
        assertThatThrownBy(() -> integratedRouteService.route(
                        Double.NaN, 126.8376, 37.5585, 126.8376, sinbanghwaParkingId(), 37.5673, 126.8270))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Long sinbanghwaParkingId() {
        return jdbcTemplate.queryForObject("SELECT id FROM tb_parking_lot WHERE name = ?", Long.class, "신방화역");
    }
}
