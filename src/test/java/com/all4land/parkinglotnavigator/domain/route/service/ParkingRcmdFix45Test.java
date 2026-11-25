package com.all4land.parkinglotnavigator.domain.route.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

import com.all4land.parkinglotnavigator.domain.parkinglot.service.ParkingLotService;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteCrdDTO;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnRouteErrCode;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParkingRcmdFix45Test {
    @Autowired
    ParkingRcmdService recommendations;

    @Autowired
    ParkingLotService parkingLots;

    @MockitoSpyBean
    DrivingRouteService driving;

    @Value("${local.server.port}")
    int port;

    private ParkingRcmdReqDTO request(int radius) {
        var start = new RouteCrdDTO(37.5586, 126.8377);
        var end = radius == 100 ? new RouteCrdDTO(37.5667867, 126.8177876) : start;
        return new ParkingRcmdReqDTO(start, start, end, (double) radius, radius == 100 ? 1 : 3);
    }

    @Test
    void caseFiveKilometres() {
        var result = recommendations.recommend(request(5000));
        assertTrue(result.candidateCount() > 10);
        assertTrue(result.evaluatedCount() > 0 && result.evaluatedCount() <= 10);
        assertEquals(
                Math.min(3, result.evaluatedCount()), result.recommendations().size());
        for (var item : result.recommendations()) {
            var s = item.summary();
            assertEquals(
                    item.totalDurationSeconds(),
                    s.departureWalkingDurationSeconds()
                            + s.drivingDurationSeconds()
                            + s.arrivalWalkingDurationSeconds());
        }
    }

    @Test
    void caseOneUnreachableParking() {
        var nearby = parkingLots.getNearbyParkingLots(37.561217, 126.844636, 500.0);
        assertTrue(nearby.size() >= 2, "Expected two parking lots near Deungchon");
        var good = nearby.getFirst();
        var bad = nearby.get(1);
        var p = new RouteCrdDTO(good.latitude(), good.longitude());

        // 한 후보의 차량 경로만 실패시킨다. 추천과 통합 경로는 실제 Spring 서비스를 호출한다.
        doThrow(new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND))
                .when(driving)
                .route(p.latitude(), p.longitude(), bad.latitude(), bad.longitude());

        var result = recommendations.recommend(new ParkingRcmdReqDTO(p, p, p, 500.0, 10));
        assertTrue(result.recommendations().stream().anyMatch(x -> x.parkingId().equals(good.id())));
        assertFalse(
                result.recommendations().stream().anyMatch(x -> x.parkingId().equals(bad.id())));
    }

    @Test
    void actualHttp() {
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(120));
        var client = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("http://127.0.0.1:" + port)
                .build();

        for (int radius : List.of(100, 5000, 5000, 5000)) {
            var req = request(radius);
            long start = System.nanoTime();
            var response = client.post()
                    .uri("/api/routes/recommendations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .toEntity(String.class);
            long millis = (System.nanoTime() - start) / 1_000_000;
            var json = JsonPath.parse(response.getBody());
            int candidates = json.read("$.data.candidateCount");
            int evaluated = json.read("$.data.evaluatedCount");
            List<?> rows = json.read("$.data.recommendations");

            assertEquals(200, response.getStatusCode().value());
            assertTrue(Boolean.TRUE.equals(json.read("$.success")));
            assertTrue(evaluated > 0 && evaluated <= Math.min(10, candidates));
            assertEquals(Math.min(req.limit(), evaluated), rows.size());
            System.out.printf(
                    "FIX45_HTTP radius=%d status=200 candidates=%d evaluated=%d returned=%d ms=%d%n",
                    radius, candidates, evaluated, rows.size(), millis);
        }
    }
}
