package com.all4land.parkinglotnavigator.domain.place.dto;

import com.all4land.parkinglotnavigator.global.response.CmmFilterDTO;
import com.all4land.parkinglotnavigator.global.response.CmmPaginationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PlaceListResDTO(
        CmmPaginationDTO pagination,
        CmmFilterDTO filter,

        @Schema(description = "Detailed places on the requested page")
        List<PlaceResDTO> list,

        @Schema(description = "Map markers for all places matching the search")
        List<PlaceMarkerResDTO> markers) {}
