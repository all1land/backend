package com.all4land.parkinglotnavigator.domain.place.service;

import com.all4land.parkinglotnavigator.domain.place.dto.PlaceListRequest;
import com.all4land.parkinglotnavigator.domain.place.dto.PlaceResponse;
import com.all4land.parkinglotnavigator.domain.place.repository.PlaceRepository;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.CommonErrorCode;
import com.all4land.parkinglotnavigator.global.response.CommonFilter;
import com.all4land.parkinglotnavigator.global.response.CommonListResponse;
import jakarta.validation.Validator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final Validator validator;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CommonListResponse<PlaceResponse> getPlaces(PlaceListRequest request) {
        if (request == null || !validator.validate(request).isEmpty()) {
            throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "장소 조회 영역과 검색 조건을 확인해주세요.");
        }
        long total = placeRepository.countInBounds(request);
        List<PlaceResponse> items = total == 0 ? List.of() : placeRepository.findInBounds(request);
        return CommonListResponse.fromPage(
                items, total, request.getPageIndex(), request.getPageSize(), appliedFilter(request));
    }

    private CommonFilter appliedFilter(PlaceListRequest request) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("keyword", request.getKeyword());
        values.put("minLat", request.getMinLat());
        values.put("minLon", request.getMinLon());
        values.put("maxLat", request.getMaxLat());
        values.put("maxLon", request.getMaxLon());
        values.put("centerLat", request.getCenterLat());
        values.put("centerLon", request.getCenterLon());
        if (request.getZoom() != null) {
            values.put("zoom", request.getZoom());
        }
        return new CommonFilter(values);
    }
}
