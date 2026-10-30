package com.all4land.parkinglotnavigator.integratedroute.controller;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationRequest;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationResponse;
import com.all4land.parkinglotnavigator.integratedroute.service.ParkingRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class ParkingRecommendationController {

    private final ParkingRecommendationService recommendationService;

    @PostMapping("/recommendations")
    public ApiResponse<ParkingRecommendationResponse> recommend(
            @Valid @RequestBody ParkingRecommendationRequest request) {
        return ApiResponse.success(CommonSuccessCode.OK, recommendationService.recommend(request));
    }
}
