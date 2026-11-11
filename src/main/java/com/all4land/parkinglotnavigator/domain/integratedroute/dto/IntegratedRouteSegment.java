package com.all4land.parkinglotnavigator.domain.integratedroute.dto;

import java.util.List;

public record IntegratedRouteSegment(
        int sequence, TravelMode mode, int distanceMeters, int durationSeconds, List<double[]> coordinates) {}
