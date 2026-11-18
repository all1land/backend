package com.all4land.parkinglotnavigator.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "개별 장소 등록 요청")
public class PlaceCreateReqDTO {

    @NotBlank @Size(max = 200) @Schema(description = "장소명", example = "마곡나루역")
    private String name;

    @NotBlank @Size(max = 50) @Schema(description = "장소 카테고리 코드", example = "SUBWAY")
    private String categoryCode;

    @Size(max = 500) @Schema(description = "도로명 주소", example = "서울특별시 강서구 마곡중앙5로 2")
    private String roadNameAddress;

    @Size(max = 500) @Schema(description = "지번 주소", example = "서울특별시 강서구 마곡동 760")
    private String lotNumberAddress;

    @Size(max = 20) @Schema(description = "우편번호", example = "07788")
    private String zipCode;

    @Size(max = 30) @Schema(description = "전화번호", example = "02-1234-5678")
    private String phone;

    @NotNull @DecimalMin("-90") @DecimalMax("90") @Schema(description = "장소 위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;

    @NotNull @DecimalMin("-180") @DecimalMax("180") @Schema(description = "장소 경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;

    @DecimalMin("-90") @DecimalMax("90") @Schema(description = "입구 위도", example = "37.5666")
    private Double entranceLatitude;

    @DecimalMin("-180") @DecimalMax("180") @Schema(description = "입구 경도", example = "126.9781")
    private Double entranceLongitude;

    @AssertTrue(message = "entranceLatitude와 entranceLongitude는 함께 전달해야 합니다.") @Schema(hidden = true)
    public boolean isValidEntranceLocation() {
        if (entranceLatitude == null && entranceLongitude == null) {
            return true;
        }
        return entranceLatitude != null
                && entranceLongitude != null
                && Double.isFinite(entranceLatitude)
                && Double.isFinite(entranceLongitude);
    }
}
