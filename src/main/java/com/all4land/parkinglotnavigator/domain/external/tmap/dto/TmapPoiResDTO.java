package com.all4land.parkinglotnavigator.domain.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapPoiResDTO(SearchPoiInfoDTO searchPoiInfo) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchPoiInfoDTO(Integer totalCount, PoisDTO pois) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PoisDTO(List<PoiDTO> poi) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PoiDTO(
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
            NewAddressListDTO newAddressList) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewAddressListDTO(List<NewAddressDTO> newAddress) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewAddressDTO(String fullAddressRoad) {}
}
