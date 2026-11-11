package com.all4land.parkinglotnavigator.domain.route.dto;

import java.util.List;

public record RouteSegment(
        int sequence, TravelMode mode, int distanceMeters, int durationSeconds, List<double[]> coordinates) {}
