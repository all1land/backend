package com.all4land.parkinglotnavigator.domain.parkinglot.controller;

import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotDetailResponse;
import com.all4land.parkinglotnavigator.domain.parkinglot.dto.ParkingLotNearbyResponse;
import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping("/{parkingId}")
    public ApiResponse<ParkingLotDetailResponse> getParkingLot(@PathVariable("parkingId") Long parkingId) {

        ParkingLotDetailResponse response = parkingLotService.getParkingLot(parkingId);

        return ApiResponse.success(EnCmmScssCode.OK, response);
    }

    @GetMapping("/nearby")
    public ApiResponse<List<ParkingLotNearbyResponse>> getNearbyParkingLots(
            @RequestParam("latitude") @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam("longitude") @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @RequestParam("radius") @Positive Double radius) {

        List<ParkingLotNearbyResponse> response = parkingLotService.getNearbyParkingLots(latitude, longitude, radius);

        return ApiResponse.success(EnCmmScssCode.OK, response);
    }
}
