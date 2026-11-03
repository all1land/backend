package com.all4land.parkinglotnavigator.place.controller;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.CommonSuccessCode;
import com.all4land.parkinglotnavigator.place.dto.PlaceSearchResponse;
import com.all4land.parkinglotnavigator.place.service.PlaceService;
import com.all4land.parkinglotnavigator.place.swagger.PlaceApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController implements PlaceApi {

    private final PlaceService placeService;

    @Override
    @GetMapping("/search")
    public ApiResponse<PlaceSearchResponse> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        PlaceSearchResponse response = placeService.search(keyword, page, count);
        return ApiResponse.success(CommonSuccessCode.OK, response);
    }
}
