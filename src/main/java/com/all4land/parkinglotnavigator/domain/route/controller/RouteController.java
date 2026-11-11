package com.all4land.parkinglotnavigator.domain.route.controller;

import com.all4land.parkinglotnavigator.domain.route.dto.RouteCoordinateRequest;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResponse;
import com.all4land.parkinglotnavigator.domain.route.service.RouteService;
import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/all")
    public ApiResponse<RouteResponse> routeByCoordinates(@Valid @RequestBody RouteCoordinateRequest request) {

        RouteResponse response = routeService.route(
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
