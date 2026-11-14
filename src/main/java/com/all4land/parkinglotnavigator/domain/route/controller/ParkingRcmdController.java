package com.all4land.parkinglotnavigator.domain.route.controller;

import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdResDTO;
import com.all4land.parkinglotnavigator.domain.route.service.ParkingRcmdService;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import com.all4land.parkinglotnavigator.swagger.ParkingRcmdApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class ParkingRcmdController implements ParkingRcmdApi {

    private final ParkingRcmdService parkingRcmdService;

    @Override
    @PostMapping("/recommendations")
    public ApiResDTO<ParkingRcmdResDTO> recommend(@Valid @RequestBody ParkingRcmdReqDTO request) {
        return ApiResDTO.success(EnCmmScssCode.OK, parkingRcmdService.recommend(request));
    }
}
