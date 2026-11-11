package com.all4land.parkinglotnavigator.domain.route;

import com.all4land.parkinglotnavigator.domain.route.dto.RouteRequest;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResponse;
import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import com.all4land.parkinglotnavigator.swagger.RouteApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes/tmap")
@RequiredArgsConstructor
public class RouteController implements RouteApi {

    private final RouteService routeService;

    @Override
    @GetMapping("/driving")
    public ApiResponse<RouteResponse> driving(@Valid @ModelAttribute RouteRequest request) {
        return ApiResponse.success(CommonSuccessCode.OK, routeService.driving(request));
    }

    @Override
    @GetMapping("/walking")
    public ApiResponse<RouteResponse> walking(@Valid @ModelAttribute RouteRequest request) {
        return ApiResponse.success(CommonSuccessCode.OK, routeService.walking(request));
    }
}
