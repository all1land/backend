package com.all4land.parkinglotnavigator.domain.place.controller;

import com.all4land.parkinglotnavigator.domain.place.dto.PlaceListRequest;
import com.all4land.parkinglotnavigator.domain.place.dto.PlaceResponse;
import com.all4land.parkinglotnavigator.domain.place.service.PlaceService;
import com.all4land.parkinglotnavigator.domain.place.swagger.PlaceApi;
import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonListResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
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
    public ApiResponse<CommonListResponse<PlaceResponse>> getPlaces(
            @Valid @ModelAttribute @ParameterObject PlaceListRequest request) {
        CommonListResponse<PlaceResponse> response = placeService.getPlaces(request);
        return ApiResponse.success(CommonSuccessCode.OK, response);
    }
}
