package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.all4land.parkinglotnavigator.domain.route.dto.RouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.TravelMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class RouteSummaryTest {

    private final RouteAssembler assembler = new RouteAssembler();

    @Test
    void summarizesDepartureWalkingDrivingAndArrivalWalking() {
        var departureWalking = List.of(segment(TravelMode.WALKING, 100, 20));
        var driving = List.of(
                segment(TravelMode.WALKING, 10, 5),
                segment(TravelMode.DRIVING, 900, 60),
                segment(TravelMode.DRIVING, 50, 5));
        var arrivalWalking = List.of(segment(TravelMode.WALKING, 200, 25));

        var response = assembler.assemble(List.of(departureWalking, driving, arrivalWalking));

        assertThat(response.totalDistanceMeters()).isEqualTo(1260);
        assertThat(response.totalDurationSeconds()).isEqualTo(115);
        assertThat(response.summary().totalDurationSeconds()).isEqualTo(115);
        assertThat(response.summary().departureWalkingDurationSeconds()).isEqualTo(25);
        assertThat(response.summary().drivingDurationSeconds()).isEqualTo(65);
        assertThat(response.summary().arrivalWalkingDurationSeconds()).isEqualTo(25);
    }

    @Test
    void returnsZeroForEverySectionWhenThereIsNoMovement() {
        var response = assembler.assemble(
                List.of(List.<RouteSegmentDTO>of(), List.<RouteSegmentDTO>of(), List.<RouteSegmentDTO>of()));

        assertThat(response.totalDurationSeconds()).isZero();
        assertThat(response.summary().totalDurationSeconds()).isZero();
        assertThat(response.summary().departureWalkingDurationSeconds()).isZero();
        assertThat(response.summary().drivingDurationSeconds()).isZero();
        assertThat(response.summary().arrivalWalkingDurationSeconds()).isZero();
    }

    private static RouteSegmentDTO segment(TravelMode mode, int distanceMeters, int durationSeconds) {
        return new RouteSegmentDTO(
                0, mode, distanceMeters, durationSeconds, List.of(new double[] {126.8377, 37.5586}, new double[] {
                    126.8376, 37.5585
                }));
    }
}
