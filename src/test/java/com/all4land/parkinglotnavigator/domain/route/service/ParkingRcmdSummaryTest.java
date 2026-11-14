package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotNearbyResDTO;
import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteCrdDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteSummaryDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParkingRcmdSummaryTest {

    @Test
    void copiesRouteSummaryToRecommendationResult() {
        ParkingLotService parkingLots = mock(ParkingLotService.class);
        RouteService routes = mock(RouteService.class);
        ParkingRcmdService service = new ParkingRcmdService(parkingLots, routes);

        var request = new ParkingRcmdReqDTO(
                new RouteCrdDTO(37.5586, 126.8377),
                new RouteCrdDTO(37.5585, 126.8376),
                new RouteCrdDTO(37.5673, 126.8270),
                1000.0,
                1);

        var parking = new ParkingLotNearbyResDTO(
                1L, "신방화역", null, 10, 37.5667867, 126.8177876, 100.0, null, null, null, null, null, null);

        var summary = new RouteSummaryDTO(120, 10, 80, 30);

        when(parkingLots.getNearbyParkingLots(37.5673, 126.8270, 1000.0)).thenReturn(List.of(parking));

        when(routes.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyLong(), anyDouble(), anyDouble()))
                .thenReturn(new RouteResDTO(List.of(), 1000, 120, summary));

        var response = service.recommend(request);

        assertThat(response.candidateCount()).isEqualTo(1);
        assertThat(response.evaluatedCount()).isEqualTo(1);
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().getFirst().summary()).isEqualTo(summary);
        assertThat(response.recommendations().getFirst().totalDurationSeconds())
                .isEqualTo(summary.totalDurationSeconds());
    }
}
