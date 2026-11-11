package com.all4land.parkinglotnavigator.domain.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParkingRcmdReqDTO(
        @NotNull @Valid RouteCrdDTO origin,
        @NotNull @Valid RouteCrdDTO departurePoi,
        @NotNull @Valid RouteCrdDTO destination,

        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("5000") Double radius,

        @NotNull @Min(1) @Max(10) Integer limit) {}
