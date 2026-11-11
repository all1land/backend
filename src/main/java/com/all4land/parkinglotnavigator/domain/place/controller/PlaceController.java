package com.all4land.parkinglotnavigator.domain.place.controller;

import com.all4land.parkinglotnavigator.domain.place.dto.PlaceListReqDTO;
import com.all4land.parkinglotnavigator.domain.place.dto.PlaceResDTO;
import com.all4land.parkinglotnavigator.domain.place.service.PlaceService;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.CmmListResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import com.all4land.parkinglotnavigator.swagger.PlaceApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController implements PlaceApi {

    private final PlaceService placeService;

    @Override
    @GetMapping("/list")
    public ApiResDTO<CmmListResDTO<PlaceResDTO>> getPlaces(
            @Valid @ModelAttribute @ParameterObject PlaceListReqDTO request) {
        CmmListResDTO<PlaceResDTO> response = placeService.getPlaces(request);
        return ApiResDTO.success(EnCmmScssCode.OK, response);
    }
}
