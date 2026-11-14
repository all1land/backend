package com.all4land.parkinglotnavigator.domain.route.controller;

import com.all4land.parkinglotnavigator.domain.route.dto.RouteCrdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResDTO;
import com.all4land.parkinglotnavigator.domain.route.service.RouteService;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import com.all4land.parkinglotnavigator.swagger.IntegratedRouteApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController implements IntegratedRouteApi {

    private final RouteService routeService;

    @Override
    @PostMapping("/all")
    public ApiResDTO<RouteResDTO> routeByCoordinates(@Valid @RequestBody RouteCrdReqDTO request) {

        RouteResDTO response = routeService.route(
                request.origin().latitude(),
                request.origin().longitude(),
                request.departurePoi().latitude(),
                request.departurePoi().longitude(),
                request.parkingId(),
                request.destination().latitude(),
                request.destination().longitude());

        return ApiResDTO.success(EnCmmScssCode.OK, response);
    }
}
