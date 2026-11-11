package com.all4land.parkinglotnavigator.domain.tmapRoute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteResponse;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TmapRouteController.class)
class TmapRouteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TmapRouteService tmapRouteService;

    @Test
    void drivingReturnsRoute() throws Exception {
        given(tmapRouteService.driving(any()))
                .willReturn(new TmapRouteResponse(1520, 420, List.of(new double[] {126.8495, 37.5509})));

        mockMvc.perform(get("/api/routes/tmap/driving")
                        .param("startLat", "37.5509")
                        .param("startLng", "126.8495")
                        .param("endLat", "37.5583")
                        .param("endLng", "126.8389"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.distanceMeters").value(1520))
                .andExpect(jsonPath("$.data.durationSeconds").value(420))
                .andExpect(jsonPath("$.data.coordinates[0][0]").value(126.8495));
    }

    @Test
    void missingCoordinateIsValidationError() throws Exception {
        mockMvc.perform(get("/api/routes/tmap/walking")
                        .param("startLat", "37.5509")
                        .param("startLng", "126.8495")
                        .param("endLat", "37.5583"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));
    }

    @Test
    void outOfRangeLatitudeIsValidationError() throws Exception {
        mockMvc.perform(get("/api/routes/tmap/driving")
                        .param("startLat", "137.5")
                        .param("startLng", "126.8495")
                        .param("endLat", "37.5583")
                        .param("endLng", "126.8389"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));
    }

    @Test
    void tmapFailureIsBadGateway() throws Exception {
        given(tmapRouteService.driving(any())).willThrow(new BusinessException(TmapRouteErrorCode.ROUTE_API_FAILED));

        mockMvc.perform(get("/api/routes/tmap/driving")
                        .param("startLat", "37.5509")
                        .param("startLng", "126.8495")
                        .param("endLat", "37.5583")
                        .param("endLng", "126.8389"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("ROUTE_502_001"));
    }
}
