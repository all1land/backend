package com.all4land.parkinglotnavigator.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import tools.jackson.databind.JsonNode;

// Tmap 경로 응답(GeoJSON FeatureCollection) 중 경로 표출에 필요한 필드만 받는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapRouteResponse(List<Feature> features) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(Geometry geometry, FeatureProperties properties) {}

    // Point는 [x, y], LineString은 [[x, y], ...]로 모양이 달라서 트리로 받아 호출하는 쪽에서 해석한다.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(String type, JsonNode coordinates) {}

    // totalDistance(m), totalTime(초)은 출발지 Feature에만 들어 있다.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FeatureProperties(Integer totalDistance, Integer totalTime) {}
}
