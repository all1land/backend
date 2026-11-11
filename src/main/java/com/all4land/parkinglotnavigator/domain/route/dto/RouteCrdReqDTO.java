package com.all4land.parkinglotnavigator.domain.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RouteCrdReqDTO(
        @NotNull @Valid RouteCrdDTO origin,
        @NotNull @Valid RouteCrdDTO departurePoi,
        @NotNull @Positive Long parkingId,
        @NotNull @Valid RouteCrdDTO destination) {}
