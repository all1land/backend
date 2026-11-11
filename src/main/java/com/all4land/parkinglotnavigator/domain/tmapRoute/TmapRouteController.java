package com.all4land.parkinglotnavigator.domain.tmapRoute;

import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteReqDTO;
import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteResDTO;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
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
    public ApiResDTO<TmapRouteResDTO> driving(@Valid @ModelAttribute TmapRouteReqDTO request) {
        return ApiResDTO.success(EnCmmScssCode.OK, tmapRouteService.driving(request));
    }

    @Override
    @GetMapping("/walking")
    public ApiResDTO<TmapRouteResDTO> walking(@Valid @ModelAttribute TmapRouteReqDTO request) {
        return ApiResDTO.success(EnCmmScssCode.OK, tmapRouteService.walking(request));
    }
}
