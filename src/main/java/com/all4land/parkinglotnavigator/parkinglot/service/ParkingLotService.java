package com.all4land.parkinglotnavigator.parkinglot.service;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.CommonErrorCode;
import com.all4land.parkinglotnavigator.parkinglot.dto.ParkingLotDetailResponse;
import com.all4land.parkinglotnavigator.parkinglot.entity.ParkingLot;
import com.all4land.parkinglotnavigator.parkinglot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLotDetailResponse getParkingLot(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository
                .findById(parkingLotId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.NOT_FOUND, "주차장 정보를 찾을 수 없습니다."));

        return ParkingLotDetailResponse.from(parkingLot);
    }
}
