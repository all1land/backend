package com.all4land.parkinglotnavigator.drivingroute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 차량 경로의 한 구간. 모든 좌표는 WGS84(EPSG:4326). */
public record DrivingRouteSegment(
        @Schema(description = "구간 시작 위도", example = "37.5585")
        double startLat,

        @Schema(description = "구간 시작 경도", example = "126.8376")
        double startLng,

        @Schema(description = "구간 끝 위도", example = "37.5586")
        double endLat,

        @Schema(description = "구간 끝 경도", example = "126.8375")
        double endLng,

        @Schema(description = "구간 거리(m)", example = "364") int distanceMeters,
        @Schema(description = "구간 소요 시간(초)", example = "26") int durationSeconds,

        @Schema(description = "구간 선형 좌표열. 각 좌표는 [경도, 위도] 순서(GeoJSON 규약). 첫 좌표는 시작점, 마지막 좌표는 끝점")
        List<double[]> coordinates) {}
