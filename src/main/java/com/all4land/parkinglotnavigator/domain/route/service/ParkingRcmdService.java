package com.all4land.parkinglotnavigator.domain.route.service;

import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdResDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdResDTO.ItemDTO;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingRcmdService {

    private static final int MAX_CANDIDATES = 50;

    private final ParkingLotService parkingLotService;
    private final RouteService routeService;

    /**
     * 반경 내 모든 후보를 평가한 뒤 전체 시간, 전체 거리, ID 순으로 추천한다.
     * 후보 경로 하나라도 실패하면 예외를 그대로 전달하며 부분 추천하지 않는다.
     * TMAP 및 실시간 주차 가능 정보는 추천 계산에 사용하지 않는다.
     */
    public ParkingRcmdResDTO recommend(ParkingRcmdReqDTO request) {
        var destination = request.destination();

        var candidates = parkingLotService.getNearbyParkingLots(
                destination.latitude(), destination.longitude(), request.radius());

        if (candidates.size() > MAX_CANDIDATES) {
            throw new BusinessException(EnCmmErrCode.BAD_REQUEST, "추천 후보가 50개를 초과했습니다. 검색 반경을 줄여주세요.");
        }

        var origin = request.origin();
        var departurePoi = request.departurePoi();
        List<ItemDTO> evaluated = new ArrayList<>();

        for (var parking : candidates) {
            var route = routeService.route(
                    origin.latitude(),
                    origin.longitude(),
                    departurePoi.latitude(),
                    departurePoi.longitude(),
                    parking.id(),
                    destination.latitude(),
                    destination.longitude());

            evaluated.add(new ItemDTO(
                    0, parking.id(), parking.name(), route.totalDistanceMeters(), route.totalDurationSeconds()));
        }

        evaluated.sort(Comparator.comparingLong(ItemDTO::totalDurationSeconds)
                .thenComparingLong(ItemDTO::totalDistanceMeters)
                .thenComparing(ItemDTO::parkingId));

        int count = Math.min(request.limit(), evaluated.size());
        List<ItemDTO> recommendations = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            var item = evaluated.get(index);
            recommendations.add(new ItemDTO(
                    index + 1, item.parkingId(), item.name(), item.totalDistanceMeters(), item.totalDurationSeconds()));
        }

        return new ParkingRcmdResDTO(candidates.size(), evaluated.size(), List.copyOf(recommendations));
    }
}
