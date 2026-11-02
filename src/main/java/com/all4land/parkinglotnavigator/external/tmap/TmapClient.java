package com.all4land.parkinglotnavigator.external.tmap;

import com.all4land.parkinglotnavigator.external.tmap.dto.TmapRouteRequest;
import com.all4land.parkinglotnavigator.external.tmap.dto.TmapRouteResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

// Tmap 경로 안내 API 호출만 담당한다. 응답 해석과 예외 변환은 호출하는 쪽(RouteService)에서 한다.
public class TmapClient {

    private static final String DRIVING_PATH = "/tmap/routes";
    private static final String PEDESTRIAN_PATH = "/tmap/routes/pedestrian";
    private static final int API_VERSION = 1;

    // 보행자 경로 API는 출발지·목적지 명칭이 필수이고 URL 인코딩된 값을 받는다.
    private static final String START_NAME = URLEncoder.encode("출발지", StandardCharsets.UTF_8);
    private static final String END_NAME = URLEncoder.encode("도착지", StandardCharsets.UTF_8);

    private final RestClient restClient;

    public TmapClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public TmapRouteResponse driving(double startLng, double startLat, double endLng, double endLat) {
        return post(DRIVING_PATH, TmapRouteRequest.driving(startLng, startLat, endLng, endLat));
    }

    public TmapRouteResponse pedestrian(double startLng, double startLat, double endLng, double endLat) {
        return post(
                PEDESTRIAN_PATH, TmapRouteRequest.pedestrian(startLng, startLat, endLng, endLat, START_NAME, END_NAME));
    }

    private TmapRouteResponse post(String path, TmapRouteRequest request) {
        return restClient
                .post()
                .uri(uriBuilder ->
                        uriBuilder.path(path).queryParam("version", API_VERSION).build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TmapRouteResponse.class);
    }
}
