package com.all4land.parkinglotnavigator.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapPoiResponse(SearchPoiInfo searchPoiInfo) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchPoiInfo(Integer totalCount, Pois pois) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pois(List<Poi> poi) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Poi(
            String id,
            String name,
            String noorLat,
            String noorLon,
            String frontLat,
            String frontLon,
            String upperAddrName,
            String middleAddrName,
            String lowerAddrName,
            String mlClass,
            String firstNo,
            String secondNo,
            String roadName,
            String firstBuildNo,
            String secondBuildNo,
            NewAddressList newAddressList) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewAddressList(List<NewAddress> newAddress) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewAddress(String fullAddressRoad) {}
}
