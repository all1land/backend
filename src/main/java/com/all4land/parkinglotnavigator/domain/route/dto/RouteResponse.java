package com.all4land.parkinglotnavigator.domain.route.dto;

import java.util.List;

public record RouteResponse(List<RouteSegment> segments, long totalDistanceMeters, long totalDurationSeconds) {}
