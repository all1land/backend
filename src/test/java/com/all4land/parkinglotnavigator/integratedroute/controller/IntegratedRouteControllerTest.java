package com.all4land.parkinglotnavigator.integratedroute.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteResponse;
import com.all4land.parkinglotnavigator.integratedroute.service.IntegratedRouteService;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IntegratedRouteController.class)
class IntegratedRouteControllerTest {

    private static final String URL = "/api/routes/all";

    private static final String REQUEST = """
            {
              "origin": {"latitude": 37.5586, "longitude": 126.8377},
              "departurePoi": {"latitude": 37.5585, "longitude": 126.8376},
              "parkingId": 1,
              "destination": {"latitude": 37.5673, "longitude": 126.8270}
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegratedRouteService integratedRouteService;

    @Test
    void delegatesCoordinatesAndReturnsCommonResponse() throws Exception {
        // HTTP 응답 형식 확인용 결과이며 실제 경로 데이터는 아니다.
        when(integratedRouteService.route(37.5586, 126.8377, 37.5585, 126.8376, 1L, 37.5673, 126.8270))
                .thenReturn(new IntegratedRouteResponse(List.of(), 0, 0));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON_200_001"))
                .andExpect(jsonPath("$.data.segments").isArray())
                .andExpect(jsonPath("$.data.totalDistanceMeters").value(0))
                .andExpect(jsonPath("$.data.totalDurationSeconds").value(0));

        verify(integratedRouteService).route(37.5586, 126.8377, 37.5585, 126.8376, 1L, 37.5673, 126.8270);
    }

    @Test
    void rejectsMissingCoordinatesBeforeCallingService() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"parkingId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));

        verifyNoInteractions(integratedRouteService);
    }

    @Test
    void rejectsOutOfRangeLatitude() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST.replace("37.5586", "91.0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));

        verifyNoInteractions(integratedRouteService);
    }

    @Test
    void rejectsInvalidParkingId() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST.replace("\"parkingId\": 1", "\"parkingId\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));

        verifyNoInteractions(integratedRouteService);
    }

    @Test
    void preservesRouteErrorWithoutPartialSuccess() throws Exception {
        when(integratedRouteService.route(37.5586, 126.8377, 37.5585, 126.8376, 1L, 37.5673, 126.8270))
                .thenThrow(new BusinessException(WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("WALKING_ROUTE_400_001"))
                .andExpect(jsonPath("$.data.segments").doesNotExist());
    }
}
