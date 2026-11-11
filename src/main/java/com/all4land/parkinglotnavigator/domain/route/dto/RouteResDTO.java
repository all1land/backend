package com.all4land.parkinglotnavigator.domain.route.dto;

import java.util.List;

public record RouteResDTO(List<RouteSegmentDTO> segments, long totalDistanceMeters, long totalDurationSeconds) {}
