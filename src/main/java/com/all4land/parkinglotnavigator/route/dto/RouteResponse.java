package com.all4land.parkinglotnavigator.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RouteResponse(
        @Schema(description = "총 거리(m)", example = "1520") int distanceMeters,
        @Schema(description = "총 소요 시간(초)", example = "420") int durationSeconds,

        @Schema(description = "경로 좌표열. 각 좌표는 [경도, 위도] 순서(GeoJSON 규약)")
        List<double[]> coordinates) {}
