package com.all4land.parkinglotnavigator.place.dto;

// poiId는 TMAP 식별자이며 tb_place의 기본 키가 아니다. 좌표계는 WGS84이다.
public record PlaceResponse(
        String poiId,
        String name,
        String categoryCode,
        String roadNameAddress,
        String lotNumberAddress,
        String zipCode,
        String phone,
        double latitude,
        double longitude,
        Double entranceLatitude,
        Double entranceLongitude) {}
