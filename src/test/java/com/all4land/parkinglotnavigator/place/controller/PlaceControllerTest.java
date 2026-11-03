package com.all4land.parkinglotnavigator.place.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.place.PlaceErrorCode;
import com.all4land.parkinglotnavigator.place.dto.PlaceResponse;
import com.all4land.parkinglotnavigator.place.dto.PlaceSearchResponse;
import com.all4land.parkinglotnavigator.place.service.PlaceService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PlaceService placeService;

    @Test
    void returnsCoordinatesInCommonResponseWithDefaultPagination() throws Exception {
        String keyword = "마곡";
        var place = new PlaceResponse("test-id", "테스트 목적지", "서울 강서구 테스트로", null, 37.56, 126.83, 37.561, 126.831);
        given(placeService.search(keyword, 1, 10)).willReturn(new PlaceSearchResponse(1, 1, List.of(place)));

        mockMvc.perform(get("/api/places/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON_200_001"))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.places[0].poiId").value("test-id"))
                .andExpect(jsonPath("$.data.places[0].latitude").value(37.56))
                .andExpect(jsonPath("$.data.places[0].longitude").value(126.83));
    }

    @ParameterizedTest
    @CsvSource({"page,0", "page,201", "page,abc", "count,0", "count,21"})
    void rejectsInvalidPagination(String name, String value) throws Exception {
        mockMvc.perform(get("/api/places/search").param("keyword", "마곡").param(name, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));
        verifyNoInteractions(placeService);
    }

    @Test
    void rejectsMissingOrBlankKeyword() throws Exception {
        mockMvc.perform(get("/api/places/search")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/places/search").param("keyword", "  ")).andExpect(status().isBadRequest());
        verifyNoInteractions(placeService);
    }

    @Test
    void rejectsOverlongKeyword() throws Exception {
        mockMvc.perform(get("/api/places/search").param("keyword", "가".repeat(101)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(placeService);
    }

    @Test
    void preservesDomainErrorCodeAndStatus() throws Exception {
        given(placeService.search("마곡", 1, 10))
                .willThrow(new BusinessException(PlaceErrorCode.PLACE_API_NOT_CONFIGURED));
        mockMvc.perform(get("/api/places/search").param("keyword", "마곡"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PLACE_503_001"));
    }
}
