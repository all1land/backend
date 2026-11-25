package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotNearbyResDTO;
import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdResDTO.ItemDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteCrdDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResDTO;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import com.all4land.parkinglotnavigator.global.response.EnRouteErrCode;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class ParkingRcmdServiceTest {

    private final ParkingLotService parking = mock(ParkingLotService.class);
    private final RouteService routes = mock(RouteService.class);
    private final ParkingRcmdService service = new ParkingRcmdService(parking, routes);

    private ParkingRcmdReqDTO request(int limit) {
        return new ParkingRcmdReqDTO(
                new RouteCrdDTO(37.5586, 126.8377),
                new RouteCrdDTO(37.5585, 126.8376),
                new RouteCrdDTO(37.5673, 126.8270),
                1000.0,
                limit);
    }

    private ParkingLotNearbyResDTO candidate(long id) {
        return new ParkingLotNearbyResDTO(
                id,
                "parking-" + id,
                null,
                10,
                37.5667867,
                126.8177876,
                (double) id,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private void candidates(List<ParkingLotNearbyResDTO> values) {
        when(parking.getNearbyParkingLots(37.5673, 126.8270, 1000.0)).thenReturn(values);
    }

    private void preparedDepartureWalking() {
        when(routes.prepareDepartureWalking(37.5586, 126.8377, 37.5585, 126.8376))
                .thenReturn(List.of());
    }

    @Test
    void evaluatesNearestTenCandidatesBeforeApplyingLimitAndSortsTies() {
        candidates(LongStream.rangeClosed(1, 12).mapToObj(this::candidate).toList());

        preparedDepartureWalking();

        when(routes.routeWithPreparedDepartureWalking(
                        anyList(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyLong(),
                        anyDouble(),
                        anyDouble()))
                .thenAnswer(invocation -> {
                    long id = invocation.getArgument(5);
                    long seconds = id == 4 ? 300 : 100;
                    long meters = id == 3 ? 200 : 100;
                    return new RouteResDTO(List.of(), meters, seconds);
                });

        var response = service.recommend(request(2));

        assertThat(response.candidateCount()).isEqualTo(12);
        assertThat(response.evaluatedCount()).isEqualTo(10);
        assertThat(response.recommendations()).extracting(ItemDTO::parkingId).containsExactly(1L, 2L);

        verify(routes, times(1)).prepareDepartureWalking(37.5586, 126.8377, 37.5585, 126.8376);

        for (long id = 11; id <= 12; id++) {
            verify(routes, never())
                    .routeWithPreparedDepartureWalking(
                            anyList(),
                            anyDouble(),
                            anyDouble(),
                            anyDouble(),
                            anyDouble(),
                            eq(id),
                            anyDouble(),
                            anyDouble());
        }
    }

    @Test
    void noCandidatesReturnsEmptyResponseWithoutRouting() {
        candidates(List.of());

        var response = service.recommend(request(3));

        assertThat(response.candidateCount()).isZero();
        assertThat(response.evaluatedCount()).isZero();
        assertThat(response.recommendations()).isEmpty();

        verifyNoInteractions(routes);
    }

    @Test
    void tooManyCandidatesAreRejectedWithoutRouting() {
        candidates(LongStream.rangeClosed(1, 51).mapToObj(this::candidate).toList());

        assertThatThrownBy(() -> service.recommend(request(3)))
                .isInstanceOfSatisfying(BusinessException.class, exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(EnCmmErrCode.BAD_REQUEST));

        verifyNoInteractions(routes);
    }

    @Test
    void skipsFailedCandidateAndReturnsSuccessfulRecommendations() {
        candidates(List.of(candidate(1), candidate(2), candidate(3)));
        preparedDepartureWalking();

        when(routes.routeWithPreparedDepartureWalking(
                        anyList(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        eq(1L),
                        anyDouble(),
                        anyDouble()))
                .thenReturn(new RouteResDTO(List.of(), 100, 100));

        when(routes.routeWithPreparedDepartureWalking(
                        anyList(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        eq(2L),
                        anyDouble(),
                        anyDouble()))
                .thenThrow(new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND));

        when(routes.routeWithPreparedDepartureWalking(
                        anyList(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        eq(3L),
                        anyDouble(),
                        anyDouble()))
                .thenReturn(new RouteResDTO(List.of(), 80, 90));

        var response = service.recommend(request(1));

        assertThat(response.candidateCount()).isEqualTo(3);
        assertThat(response.evaluatedCount()).isEqualTo(2);
        assertThat(response.recommendations()).extracting(ItemDTO::parkingId).containsExactly(3L);
    }

    @Test
    void propagatesFailureWhenAllEvaluatedCandidatesFail() {
        candidates(List.of(candidate(1), candidate(2)));
        preparedDepartureWalking();

        var failure = new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND);

        when(routes.routeWithPreparedDepartureWalking(
                        anyList(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyLong(),
                        anyDouble(),
                        anyDouble()))
                .thenThrow(failure);

        assertThatThrownBy(() -> service.recommend(request(1))).isSameAs(failure);
    }
}
