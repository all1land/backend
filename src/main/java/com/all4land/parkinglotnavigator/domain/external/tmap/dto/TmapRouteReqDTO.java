package com.all4land.parkinglotnavigator.domain.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// Tmap은 X가 경도(lng), Y가 위도(lat)다. 좌표계는 기본값(WGS84GEO)을 쓴다.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TmapRouteReqDTO(
        double startX, double startY, double endX, double endY, String startName, String endName) {

    public static TmapRouteReqDTO driving(double startLng, double startLat, double endLng, double endLat) {
        return new TmapRouteReqDTO(startLng, startLat, endLng, endLat, null, null);
    }

    public static TmapRouteReqDTO pedestrian(
            double startLng, double startLat, double endLng, double endLat, String startName, String endName) {
        return new TmapRouteReqDTO(startLng, startLat, endLng, endLat, startName, endName);
    }
}
