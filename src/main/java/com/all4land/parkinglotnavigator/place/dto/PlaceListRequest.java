package com.all4land.parkinglotnavigator.place.dto;

import com.all4land.parkinglotnavigator.global.request.CommonMapRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

@Schema(description = "DB 장소의 지도 영역·검색 조건·페이지 요청")
public class PlaceListRequest extends CommonMapRequest {

    @Override
    @Schema(
            description = "선택 검색어 (장소명 또는 도로명 주소에 포함, 양끝 공백 제거)",
            example = "마곡",
            minLength = 0,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100) public String getKeyword() {
        return super.getKeyword();
    }

    @Override
    @Schema(description = "현재 페이지 (1부터 시작)", example = "1", defaultValue = "1", minimum = "1", maximum = "200")
    @Max(200) public int getPageIndex() {
        return super.getPageIndex();
    }

    @Override
    @Schema(description = "페이지당 항목 수", example = "10", defaultValue = "10", minimum = "1", maximum = "20")
    @Max(20) public int getPageSize() {
        return super.getPageSize();
    }
}
