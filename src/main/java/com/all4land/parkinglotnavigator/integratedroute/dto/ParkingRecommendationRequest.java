package com.all4land.parkinglotnavigator.integratedroute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParkingRecommendationRequest(
        @NotNull @Valid RouteCoordinate origin,
        @NotNull @Valid RouteCoordinate departurePoi,
        @NotNull @Valid RouteCoordinate destination,

        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("5000") Double radius,

        @NotNull @Min(1) @Max(10) Integer limit) {}
