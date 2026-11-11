package com.all4land.parkinglotnavigator.domain.route.dto;

import java.util.List;

public record ParkingRcmdResDTO(int candidateCount, int evaluatedCount, List<ItemDTO> recommendations) {

    public record ItemDTO(int rank, Long parkingId, String name, long totalDistanceMeters, long totalDurationSeconds) {}
}
