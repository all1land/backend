package com.all4land.parkinglotnavigator.route;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.all4land.parkinglotnavigator.external.tmap.TmapClient;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.route.dto.RouteRequest;
import com.all4land.parkinglotnavigator.route.dto.RouteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RouteServiceTest {

    private static final String BASE_URL = "https://apis.openapi.sk.com";
    private static final RouteRequest REQUEST = new RouteRequest(37.5509, 126.8495, 37.5583, 126.8389);

    // Tmap 문서의 응답 예시 형태: 출발지 Point(요약 포함) → LineString 구간들 → 도착지 Point
    private static final String ROUTE_JSON = """
            {
              "type": "FeatureCollection",
              "features": [
                {"type": "Feature",
                 "geometry": {"type": "Point", "coordinates": [126.8495, 37.5509]},
                 "properties": {"totalDistance": 1520, "totalTime": 420, "pointType": "S"}},
                {"type": "Feature",
                 "geometry": {"type": "LineString", "coordinates": [[126.8495, 37.5509], [126.8450, 37.5530]]},
                 "properties": {"index": 1, "distance": 800, "time": 200}},
                {"type": "Feature",
                 "geometry": {"type": "Point", "coordinates": [126.8450, 37.5530]},
                 "properties": {"index": 2, "pointType": "N"}},
                {"type": "Feature",
                 "geometry": {"type": "LineString", "coordinates": [[126.8450, 37.5530], [126.8389, 37.5583]]},
                 "properties": {"index": 3, "distance": 720, "time": 220}},
                {"type": "Feature",
                 "geometry": {"type": "Point", "coordinates": [126.8389, 37.5583]},
                 "properties": {"index": 4, "pointType": "E"}}
              ]
            }
            """;

    private MockRestServiceServer server;
    private RouteService routeService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL).defaultHeader("appKey", "test-key");
        server = MockRestServiceServer.bindTo(builder).build();
        routeService = new RouteService(new TmapClient(builder.build()));
    }

    @Test
    void drivingConvertsTmapResponse() {
        server.expect(requestTo(BASE_URL + "/tmap/routes?version=1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("appKey", "test-key"))
                .andExpect(jsonPath("$.startX").value(126.8495))
                .andExpect(jsonPath("$.startY").value(37.5509))
                .andExpect(jsonPath("$.startName").doesNotExist())
                .andRespond(withSuccess(ROUTE_JSON, MediaType.APPLICATION_JSON));

        RouteResponse route = routeService.driving(REQUEST);

        assertThat(route.distanceMeters()).isEqualTo(1520);
        assertThat(route.durationSeconds()).isEqualTo(420);
        // 구간 경계의 중복 좌표 [126.8450, 37.5530]는 한 번만 들어간다.
        assertThat(route.coordinates())
                .containsExactly(new double[] {126.8495, 37.5509}, new double[] {126.8450, 37.5530}, new double[] {
                    126.8389, 37.5583
                });
        server.verify();
    }

    @Test
    void walkingSendsRequiredNames() {
        server.expect(requestTo(BASE_URL + "/tmap/routes/pedestrian?version=1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.startName").value("%EC%B6%9C%EB%B0%9C%EC%A7%80"))
                .andExpect(jsonPath("$.endName").value("%EB%8F%84%EC%B0%A9%EC%A7%80"))
                .andRespond(withSuccess(ROUTE_JSON, MediaType.APPLICATION_JSON));

        assertThat(routeService.walking(REQUEST).durationSeconds()).isEqualTo(420);
        server.verify();
    }

    @Test
    void tmapBadRequestBecomesRouteNotAvailable() {
        server.expect(requestTo(BASE_URL + "/tmap/routes?version=1")).andRespond(withBadRequest());

        assertThatThrownBy(() -> routeService.driving(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RouteErrorCode.ROUTE_NOT_AVAILABLE);
    }

    @Test
    void tmapServerErrorBecomesApiFailed() {
        server.expect(requestTo(BASE_URL + "/tmap/routes?version=1")).andRespond(withServerError());

        assertThatThrownBy(() -> routeService.driving(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RouteErrorCode.ROUTE_API_FAILED);
    }

    @Test
    void responseWithoutSummaryBecomesApiFailed() {
        server.expect(requestTo(BASE_URL + "/tmap/routes?version=1"))
                .andRespond(
                        withSuccess("{\"type\":\"FeatureCollection\",\"features\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> routeService.driving(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RouteErrorCode.ROUTE_API_FAILED);
    }
}
