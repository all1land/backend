package com.all4land.parkinglotnavigator.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// id는 tb_place의 기본 키이다. 좌표계는 WGS84이다.
@Schema(description = "장소 정보 (좌표계 WGS84)")
public record PlaceResponse(
        @Schema(description = "DB 장소 식별자", example = "1") Long id,

        @Schema(description = "장소명", example = "마곡 목적지") String name,

        @Schema(description = "도로명 주소 (없으면 null)", example = "서울 강서구 예시로 1")
        String roadNameAddress,

        @Schema(description = "지번 주소", example = "서울 강서구 마곡동 1")
        String lotNumberAddress,

        @Schema(description = "지도 표시 위도", example = "37.56") double latitude,

        @Schema(description = "지도 표시 경도", example = "126.83")
        double longitude,

        @Schema(description = "입구 위도 (유효한 입구 좌표가 없으면 null)", example = "37.561")
        Double entranceLatitude,

        @Schema(description = "입구 경도 (유효한 입구 좌표가 없으면 null)", example = "126.831")
        Double entranceLongitude) {}
