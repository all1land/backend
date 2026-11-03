package com.all4land.parkinglotnavigator.place.swagger;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.place.dto.PlaceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Tag(name = "Place", description = "서울 강서구 목적지 검색 API")
public interface PlaceApi {

    @Operation(
            summary = "장소 검색 및 자동완성 후보 조회",
            description = "TMAP에서 최대 100개씩 받은 서울 강서구 후보를 이름 완전 일치, 접두어 일치, 나머지 순으로 반환한다. "
                    + "같은 순위에서는 TMAP 순서를 유지하며, 입력 중에는 page=1로 호출한다. "
                    + "keyword는 공백 제거 후 1~100자, page는 1~200, count는 1~20이다. "
                    + "latitude/longitude는 WGS84 지도 표시 좌표이며 입구 좌표는 entranceLatitude/entranceLongitude이다.")
    ApiResponse<PlaceSearchResponse> search(
            @NotBlank @Size(max = 100) String keyword, @Min(1) @Max(200) int page, @Min(1) @Max(20) int count);
}
