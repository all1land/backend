package com.all4land.parkinglotnavigator.integratedroute.service;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.CommonErrorCode;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationRequest;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationResponse;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationResponse.Item;
import com.all4land.parkinglotnavigator.parkinglot.service.ParkingLotService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingRecommendationService {

    private static final int MAX_CANDIDATES = 50;

    private final ParkingLotService parkingLotService;
    private final IntegratedRouteService integratedRouteService;

    /**
     * 반경 내 모든 후보를 평가한 뒤 전체 시간, 전체 거리, ID 순으로 추천한다.
     * 후보 경로 하나라도 실패하면 예외를 그대로 전달하며 부분 추천하지 않는다.
     * TMAP 및 실시간 주차 가능 정보는 추천 계산에 사용하지 않는다.
     */
    public ParkingRecommendationResponse recommend(ParkingRecommendationRequest request) {
        var destination = request.destination();

        var candidates = parkingLotService.getNearbyParkingLots(
                destination.latitude(), destination.longitude(), request.radius());

        if (candidates.size() > MAX_CANDIDATES) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "추천 후보가 50개를 초과했습니다. 검색 반경을 줄여주세요.");
        }

        var origin = request.origin();
        var departurePoi = request.departurePoi();
        List<Item> evaluated = new ArrayList<>();

        for (var parking : candidates) {
            var route = integratedRouteService.route(
                    origin.latitude(),
                    origin.longitude(),
                    departurePoi.latitude(),
                    departurePoi.longitude(),
                    parking.id(),
                    destination.latitude(),
                    destination.longitude());

            evaluated.add(new Item(
                    0, parking.id(), parking.name(), route.totalDistanceMeters(), route.totalDurationSeconds()));
        }

        evaluated.sort(Comparator.comparingLong(Item::totalDurationSeconds)
                .thenComparingLong(Item::totalDistanceMeters)
                .thenComparing(Item::parkingId));

        int count = Math.min(request.limit(), evaluated.size());
        List<Item> recommendations = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            var item = evaluated.get(index);
            recommendations.add(new Item(
                    index + 1, item.parkingId(), item.name(), item.totalDistanceMeters(), item.totalDurationSeconds()));
        }

        return new ParkingRecommendationResponse(candidates.size(), evaluated.size(), List.copyOf(recommendations));
    }
}
