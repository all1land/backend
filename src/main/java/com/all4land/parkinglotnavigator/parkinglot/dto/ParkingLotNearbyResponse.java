package com.all4land.parkinglotnavigator.parkinglot.dto;

public record ParkingLotNearbyResponse(
        Long id,
        String name,
        String roadNameAddress,
        Integer totalSpaces,
        Double latitude,
        Double longitude,
        Double distanceMeters) {}
