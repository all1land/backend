package com.all4land.parkinglotnavigator.domain.parkinglot.service;

import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotDetailResDTO;
import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotNearbyResDTO;
import com.all4land.parkinglotnavigator.domain.parkinglot.repository.NearbyParkingLotProjection;
import com.all4land.parkinglotnavigator.domain.parkinglot.repository.ParkingLotDetailProjection;
import com.all4land.parkinglotnavigator.domain.parkinglot.repository.ParkingLotRepository;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLotDetailResDTO getParkingLot(Long parkingLotId) {
        ParkingLotDetailProjection parkingLot = parkingLotRepository
                .findParkingLotDetail(parkingLotId)
                .orElseThrow(() -> new BusinessException(EnCmmErrCode.NOT_FOUND, "주차장 정보를 찾을 수 없습니다."));

        return ParkingLotDetailResDTO.from(parkingLot);
    }

    public List<ParkingLotNearbyResDTO> getNearbyParkingLots(Double latitude, Double longitude, Double radius) {

        List<NearbyParkingLotProjection> parkingLots =
                parkingLotRepository.findNearbyParkingLots(latitude, longitude, radius);

        return parkingLots.stream()
                .map(parkingLot -> new ParkingLotNearbyResDTO(
                        parkingLot.getId(),
                        parkingLot.getName(),
                        parkingLot.getRoadNameAddress(),
                        parkingLot.getTotalSpaces(),
                        parkingLot.getLatitude(),
                        parkingLot.getLongitude(),
                        parkingLot.getDistance(),
                        parkingLot.getPhone(),
                        parkingLot.getLotNumberAddress(),
                        parkingLot.getCategoryCode(),
                        parkingLot.getCategoryName(),
                        parkingLot.getParkingTypeCode(),
                        parkingLot.getParkingTypeName()))
                .toList();
    }
}
