package com.all4land.parkinglotnavigator.domain.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import tools.jackson.databind.JsonNode;

// Tmap 경로 응답(GeoJSON FeatureCollection) 중 경로 표출에 필요한 필드만 받는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapRouteResDTO(List<FeatureDTO> features) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FeatureDTO(GeometryDTO geometry, FeaturePropertiesDTO properties) {}

    // Point는 [x, y], LineString은 [[x, y], ...]로 모양이 달라서 트리로 받아 호출하는 쪽에서 해석한다.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeometryDTO(String type, JsonNode coordinates) {}

    // totalDistance(m), totalTime(초)은 출발지 Feature에만 들어 있다.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FeaturePropertiesDTO(Integer totalDistance, Integer totalTime) {}
}
