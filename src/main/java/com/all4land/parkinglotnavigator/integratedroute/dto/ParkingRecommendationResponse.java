package com.all4land.parkinglotnavigator.integratedroute.dto;

import java.util.List;

public record ParkingRecommendationResponse(int candidateCount, int evaluatedCount, List<Item> recommendations) {

    public record Item(int rank, Long parkingId, String name, long totalDistanceMeters, long totalDurationSeconds) {}
}
