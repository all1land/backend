package com.all4land.parkinglotnavigator.integratedroute.service;

import com.all4land.parkinglotnavigator.drivingroute.service.DrivingRouteService;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteResponse;
import com.all4land.parkinglotnavigator.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.walkingroute.service.WalkingRouteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntegratedRouteService {

    private static final double COORDINATE_TOLERANCE = 1e-9;

    private final ParkingLotService parkingLotService;
    private final WalkingRouteService walkingRouteService;
    private final DrivingRouteService drivingRouteService;
    private final IntegratedRouteAssembler assembler;

    /**
     * 사용자 출발지에서 출발 측 POI와 주차장을 거쳐 목적지까지 이동하는 경로를 반환한다.
     * 모든 입력 좌표는 WGS84이며 인자 순서는 위도, 경도이다.
     * 출발 측 POI는 호출자가 조회하여 전달한다.
     */
    public IntegratedRouteResponse route(
            double originLat,
            double originLng,
            double departurePoiLat,
            double departurePoiLng,
            Long parkingId,
            double destinationLat,
            double destinationLng) {

        validateCoordinate(originLat, originLng);
        validateCoordinate(departurePoiLat, departurePoiLng);
        validateCoordinate(destinationLat, destinationLng);

        if (parkingId == null || parkingId <= 0) {
            throw new IllegalArgumentException("주차장 ID는 양수여야 합니다.");
        }

        var parking = parkingLotService.getParkingLot(parkingId);

        if (parking.latitude() == null || parking.longitude() == null) {
            throw new IllegalStateException("주차장 좌표가 없습니다.");
        }

        double parkingLat = parking.latitude();
        double parkingLng = parking.longitude();
        validateCoordinate(parkingLat, parkingLng);

        var firstWalking = walkingRouteService.route(originLat, originLng, departurePoiLat, departurePoiLng);

        var driving = drivingRouteService.route(departurePoiLat, departurePoiLng, parkingLat, parkingLng);

        var lastWalking = walkingRouteService.route(parkingLat, parkingLng, destinationLat, destinationLng);

        var response = assembler.assemble(
                List.of(assembler.walking(firstWalking), assembler.driving(driving), assembler.walking(lastWalking)));

        validateConnection(response, originLat, originLng, destinationLat, destinationLng);

        return response;
    }

    private static void validateCoordinate(double latitude, double longitude) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {
            throw new IllegalArgumentException("위도 또는 경도가 올바르지 않습니다.");
        }
    }

    private static void validateConnection(
            IntegratedRouteResponse response,
            double originLat,
            double originLng,
            double destinationLat,
            double destinationLng) {

        double[] expected = {originLng, originLat};

        for (var segment : response.segments()) {
            var coordinates = segment.coordinates();

            if (coordinates == null || coordinates.size() < 2) {
                throw new IllegalStateException("경로 구간의 좌표열이 올바르지 않습니다.");
            }

            for (double[] coordinate : coordinates) {
                if (coordinate == null || coordinate.length != 2) {
                    throw new IllegalStateException("경로 좌표는 경도와 위도 쌍이어야 합니다.");
                }

                if (!Double.isFinite(coordinate[0])
                        || !Double.isFinite(coordinate[1])
                        || Math.abs(coordinate[0]) > 180
                        || Math.abs(coordinate[1]) > 90) {
                    throw new IllegalStateException("경로 좌표의 값이 올바르지 않습니다.");
                }
            }

            if (segment.distanceMeters() < 0 || segment.durationSeconds() < 0) {
                throw new IllegalStateException("경로 거리와 시간은 음수일 수 없습니다.");
            }

            requireSameCoordinate(expected, coordinates.getFirst());
            expected = coordinates.getLast();
        }

        requireSameCoordinate(expected, new double[] {destinationLng, destinationLat});
    }

    private static void requireSameCoordinate(double[] expected, double[] actual) {
        if (Math.abs(expected[0] - actual[0]) > COORDINATE_TOLERANCE
                || Math.abs(expected[1] - actual[1]) > COORDINATE_TOLERANCE) {
            throw new IllegalStateException("경로 구간의 연결 좌표가 일치하지 않습니다.");
        }
    }
}
