package com.all4land.parkinglotnavigator.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RouteRequest(
        @Schema(description = "출발지 위도", example = "37.5509") @NotNull @DecimalMin("-90") @DecimalMax("90") Double startLat,

        @Schema(description = "출발지 경도", example = "126.8495") @NotNull @DecimalMin("-180") @DecimalMax("180") Double startLng,

        @Schema(description = "도착지 위도", example = "37.5583") @NotNull @DecimalMin("-90") @DecimalMax("90") Double endLat,

        @Schema(description = "도착지 경도", example = "126.8389") @NotNull @DecimalMin("-180") @DecimalMax("180") Double endLng) {}
