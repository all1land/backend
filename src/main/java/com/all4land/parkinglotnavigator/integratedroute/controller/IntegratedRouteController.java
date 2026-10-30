package com.all4land.parkinglotnavigator.integratedroute.controller;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteCoordinateRequest;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteResponse;
import com.all4land.parkinglotnavigator.integratedroute.service.IntegratedRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class IntegratedRouteController {

    private final IntegratedRouteService integratedRouteService;

    @PostMapping("/all")
    public ApiResponse<IntegratedRouteResponse> routeByCoordinates(
            @Valid @RequestBody IntegratedRouteCoordinateRequest request) {

        IntegratedRouteResponse response = integratedRouteService.route(
                request.origin().latitude(),
                request.origin().longitude(),
                request.departurePoi().latitude(),
                request.departurePoi().longitude(),
                request.parkingId(),
                request.destination().latitude(),
                request.destination().longitude());

        return ApiResponse.success(CommonSuccessCode.OK, response);
    }
}
