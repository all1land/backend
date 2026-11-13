package com.all4land.parkinglotnavigator.domain.place.dto;

// DB 장소 조회 응답의 poiId는 tb_place의 기본 키를 문자열로 반환한다. 좌표계는 WGS84이다.
public record PlaceResDTO(
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
