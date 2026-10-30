package com.all4land.parkinglotnavigator.place.dto;

import java.util.List;

public record PlaceSearchResponse(int page, int totalCount, List<PlaceResponse> places) {}
