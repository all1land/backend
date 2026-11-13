package com.all4land.parkinglotnavigator.domain.place.service;

import com.all4land.parkinglotnavigator.domain.place.dto.PlaceListReqDTO;
import com.all4land.parkinglotnavigator.domain.place.dto.PlaceResDTO;
import com.all4land.parkinglotnavigator.domain.place.repository.PlaceRepository;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.CmmFilterDTO;
import com.all4land.parkinglotnavigator.global.response.CmmListResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
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
    public CmmListResDTO<PlaceResDTO> getPlaces(PlaceListReqDTO request) {
        if (request == null || !validator.validate(request).isEmpty()) {
            throw new BusinessException(EnCmmErrCode.VALIDATION_ERROR, "장소 조회 영역과 검색 조건을 확인해주세요.");
        }
        long total = placeRepository.countInBounds(request);
        List<PlaceResDTO> items = total == 0 ? List.of() : placeRepository.findInBounds(request);
        return CmmListResDTO.fromPage(
                items, total, request.getPageIndex(), request.getPageSize(), appliedFilter(request));
    }

    private CmmFilterDTO appliedFilter(PlaceListReqDTO request) {
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
        return new CmmFilterDTO(values);
    }

    @Transactional(readOnly = true)
    public PlaceResDTO getPlace(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(EnCmmErrCode.VALIDATION_ERROR, "장소 ID는 양수여야 합니다.");
        }
        return placeRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(EnCmmErrCode.NOT_FOUND, "장소 정보를 찾을 수 없습니다."));
    }
}
