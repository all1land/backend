package com.all4land.parkinglotnavigator.parkinglot.controller;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import com.all4land.parkinglotnavigator.parkinglot.dto.ParkingLotDetailResponse;
import com.all4land.parkinglotnavigator.parkinglot.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping("/{parkingLotId}")
    public ApiResponse<ParkingLotDetailResponse> getParkingLot(
        @PathVariable("parkingLotId") Long parkingLotId) {

        ParkingLotDetailResponse response =
            parkingLotService.getParkingLot(parkingLotId);

        return ApiResponse.success(
            CommonSuccessCode.OK,
            response
        );
    }
}
