package com.all4land.parkinglotnavigator.integratedroute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IntegratedRouteCoordinateRequest(
        @NotNull @Valid RouteCoordinate origin,
        @NotNull @Valid RouteCoordinate departurePoi,
        @NotNull @Positive Long parkingId,
        @NotNull @Valid RouteCoordinate destination) {}
