package com.all4land.parkinglotnavigator.integratedroute.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.all4land.parkinglotnavigator.drivingroute.DrivingRouteErrorCode;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.BaseErrorCode;
import com.all4land.parkinglotnavigator.integratedroute.dto.ParkingRecommendationResponse;
import com.all4land.parkinglotnavigator.integratedroute.service.ParkingRecommendationService;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ParkingRecommendationController.class)
class ParkingRecommendationControllerTest {

    private static final String URL = "/api/routes/recommendations";
    private static final String REQUEST = """
            {
              "origin":{"latitude":37.5586,"longitude":126.8377},
              "departurePoi":{"latitude":37.5585,"longitude":126.8376},
              "destination":{"latitude":37.5673,"longitude":126.8270},
              "radius":1000,
              "limit":3
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingRecommendationService service;

    @Test
    void returnsCommonResponse() throws Exception {
        when(service.recommend(any())).thenReturn(new ParkingRecommendationResponse(0, 0, List.of()));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendations").isEmpty());
    }

    @Test
    void validatesNestedCoordinatesRadiusAndLimit() throws Exception {
        var invalid = List.of(
                "{}",
                REQUEST.replace("37.5586", "91"),
                REQUEST.replace("\"radius\":1000", "\"radius\":0"),
                REQUEST.replace("\"radius\":1000", "\"radius\":5001"),
                REQUEST.replace("\"limit\":3", "\"limit\":0"),
                REQUEST.replace("\"limit\":3", "\"limit\":11"));

        for (String body : invalid) {
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        verifyNoInteractions(service);
    }

    @Test
    void preservesWalkingAndDrivingErrorsWithoutPartialRecommendations() throws Exception {
        List<BaseErrorCode> errors = List.of(
                WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY,
                WalkingRouteErrorCode.WALKING_ROUTE_NOT_FOUND,
                DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY,
                DrivingRouteErrorCode.DRIVING_ROUTE_NOT_FOUND);

        for (var error : errors) {
            reset(service);
            when(service.recommend(any())).thenThrow(new BusinessException(error));

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                    .andExpect(status().is(error.getHttpStatus().value()))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(error.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }
}
