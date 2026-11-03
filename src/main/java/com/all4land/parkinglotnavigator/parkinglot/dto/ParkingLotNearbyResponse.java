package com.all4land.parkinglotnavigator.parkinglot.dto;

public record ParkingLotNearbyResponse(
        Long id,
        String name,
        String roadNameAddress,
        Integer totalSpaces,
        Double latitude,
        Double longitude,
        Double distanceMeters,
        String phone,
        String lotNumberAddress,
        String categoryCode,
        String categoryName,
        String parkingTypeCode,
        String parkingTypeName) {}
