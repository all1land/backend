package com.all4land.parkinglotnavigator.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Minimal place information for a map marker")
public record PlaceMarkerResDTO(String poiId, String categoryCode, double latitude, double longitude) {}
