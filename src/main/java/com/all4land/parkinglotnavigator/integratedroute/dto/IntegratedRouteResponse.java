package com.all4land.parkinglotnavigator.integratedroute.dto;

import java.util.List;

public record IntegratedRouteResponse(
        List<IntegratedRouteSegment> segments, long totalDistanceMeters, long totalDurationSeconds) {}
