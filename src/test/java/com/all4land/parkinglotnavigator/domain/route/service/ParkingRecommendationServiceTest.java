package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotNearbyResponse;
import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.global.response.EnRouteErrCode;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRecommendationRequest;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRecommendationResponse.Item;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteCoordinate;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResponse;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class ParkingRecommendationServiceTest {

    private final ParkingLotService parking = mock(ParkingLotService.class);
    private final RouteService routes = mock(RouteService.class);
    private final ParkingRecommendationService service = new ParkingRecommendationService(parking, routes);

    private ParkingRecommendationRequest request(int limit) {
        return new ParkingRecommendationRequest(
                new RouteCoordinate(37.5586, 126.8377),
                new RouteCoordinate(37.5585, 126.8376),
                new RouteCoordinate(37.5673, 126.8270),
                1000.0,
                limit);
    }

    private ParkingLotNearbyResponse candidate(long id) {
        return new ParkingLotNearbyResponse(
                id, "parking-" + id, null, 10, 37.5667867, 126.8177876, 100.0, null, null, null, null, null, null);
    }

    private void candidates(List<ParkingLotNearbyResponse> values) {
        when(parking.getNearbyParkingLots(37.5673, 126.8270, 1000.0)).thenReturn(values);
    }

    @Test
    void evaluatesAllCandidatesBeforeApplyingLimitAndSortsTies() {
        candidates(List.of(candidate(4), candidate(3), candidate(2), candidate(1)));

        when(routes.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyLong(), anyDouble(), anyDouble()))
                .thenAnswer(invocation -> {
                    long id = invocation.getArgument(4);
                    long seconds = id == 4 ? 300 : 100;
                    long meters = id == 3 ? 200 : 100;
                    return new RouteResponse(List.of(), meters, seconds);
                });

        var response = service.recommend(request(2));

        assertThat(response.candidateCount()).isEqualTo(4);
        assertThat(response.evaluatedCount()).isEqualTo(4);
        assertThat(response.recommendations()).extracting(Item::parkingId).containsExactly(1L, 2L);
        assertThat(response.recommendations()).extracting(Item::rank).containsExactly(1, 2);

        for (long id = 1; id <= 4; id++) {
            verify(routes).route(37.5586, 126.8377, 37.5585, 126.8376, id, 37.5673, 126.8270);
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
    void tooManyCandidatesAreRejectedWithoutTruncating() {
        candidates(LongStream.rangeClosed(1, 51).mapToObj(this::candidate).toList());

        assertThatThrownBy(() -> service.recommend(request(3)))
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode())
                        .isEqualTo(EnCmmErrCode.BAD_REQUEST));

        verifyNoInteractions(routes);
    }

    @Test
    void routeFailureIsPropagatedEvenAfterEarlierCandidateSucceeded() {
        candidates(List.of(candidate(1), candidate(2), candidate(3)));

        when(routes.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(1L), anyDouble(), anyDouble()))
                .thenReturn(new RouteResponse(List.of(), 100, 100));

        var failure = new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND);
        when(routes.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(2L), anyDouble(), anyDouble()))
                .thenThrow(failure);

        assertThatThrownBy(() -> service.recommend(request(1))).isSameAs(failure);

        verify(routes, never())
                .route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(3L), anyDouble(), anyDouble());
    }
}
