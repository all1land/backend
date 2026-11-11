package com.all4land.parkinglotnavigator.domain.tmapRoute;

import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteRequest;
import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteResponse;
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
public class TmapRouteController implements RouteApi {

    private final TmapRouteService tmapRouteService;

    @Override
    @GetMapping("/driving")
    public ApiResponse<TmapRouteResponse> driving(@Valid @ModelAttribute TmapRouteRequest request) {
        return ApiResponse.success(CommonSuccessCode.OK, tmapRouteService.driving(request));
    }

    @Override
    @GetMapping("/walking")
    public ApiResponse<TmapRouteResponse> walking(@Valid @ModelAttribute TmapRouteRequest request) {
        return ApiResponse.success(CommonSuccessCode.OK, tmapRouteService.walking(request));
    }
}
