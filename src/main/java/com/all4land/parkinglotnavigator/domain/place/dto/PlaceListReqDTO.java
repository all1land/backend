package com.all4land.parkinglotnavigator.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DB 장소의 지도 영역·검색 조건·페이지 요청")
public class PlaceListReqDTO {

    @Schema(description = "현재 페이지 (1부터 시작)", defaultValue = "1")
    @Min(1) private int pageIndex = 1;

    @Schema(description = "페이지당 항목 수", defaultValue = "10")
    @Min(1) private int pageSize = 10;

    @Schema(description = "검색어", example = "마곡")
    @Setter(AccessLevel.NONE)
    private String keyword = "";

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword.strip();
    }

    @DecimalMin("-90") @DecimalMax("90") @Schema(description = "지도 중심 위도 (선택, centerLon과 함께 전달)", example = "37.56")
    private Double centerLat;

    @DecimalMin("-180") @DecimalMax("180") @Schema(description = "지도 중심 경도 (선택, centerLat과 함께 전달)", example = "126.83")
    private Double centerLon;

    @Min(0) @Schema(description = "지도 확대 수준 (선택, 조회 영역은 경계값으로 결정)", example = "15", minimum = "0")
    private Integer zoom;

    @NotNull @DecimalMin("-90") @DecimalMax("90") @Schema(description = "영역의 최소 위도", example = "37.55", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double minLat;

    @NotNull @DecimalMin("-180") @DecimalMax("180") @Schema(description = "영역의 최소 경도", example = "126.82", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double minLon;

    @NotNull @DecimalMin("-90") @DecimalMax("90") @Schema(description = "영역의 최대 위도", example = "37.58", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double maxLat;

    @NotNull @DecimalMin("-180") @DecimalMax("180") @Schema(description = "영역의 최대 경도", example = "126.85", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double maxLon;

    @AssertTrue(message = "유한한 경계값과 minLat < maxLat, minLon < maxLon 조건을 확인해주세요.") @Schema(hidden = true)
    public boolean isValidBounds() {
        if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
            return true;
        }
        return Double.isFinite(minLat)
                && Double.isFinite(minLon)
                && Double.isFinite(maxLat)
                && Double.isFinite(maxLon)
                && minLat < maxLat
                && minLon < maxLon;
    }

    @AssertTrue(message = "centerLat과 centerLon은 함께 전달해야 하며 유한한 좌표여야 합니다.") @Schema(hidden = true)
    public boolean isValidCenter() {
        if (centerLat == null && centerLon == null) {
            return true;
        }
        return centerLat != null && centerLon != null && Double.isFinite(centerLat) && Double.isFinite(centerLon);
    }

    @Schema(
            description = "선택 검색어 (장소명 또는 도로명 주소에 포함, 양끝 공백 제거)",
            example = "마곡",
            minLength = 0,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100) public String getKeyword() {
        return keyword;
    }

    @Schema(description = "현재 페이지 (1부터 시작)", example = "1", defaultValue = "1", minimum = "1", maximum = "200")
    @Max(200) public int getPageIndex() {
        return pageIndex;
    }

    @Schema(description = "페이지당 항목 수", example = "10", defaultValue = "10", minimum = "1", maximum = "20")
    @Max(20) public int getPageSize() {
        return pageSize;
    }
}
