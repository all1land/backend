package com.all4land.parkinglotnavigator.domain.tmapRoute;

import com.all4land.parkinglotnavigator.domain.external.tmap.TmapClient;
import com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO.FeatureDTO;
import com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO.FeaturePropertiesDTO;
import com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO.GeometryDTO;
import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteReqDTO;
import com.all4land.parkinglotnavigator.domain.tmapRoute.dto.TmapRouteResDTO;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmapRouteService {

    private static final String LINE_STRING = "LineString";

    private final TmapClient tmapClient;

    public TmapRouteResDTO driving(TmapRouteReqDTO request) {
        return toRoute(call(
                () -> tmapClient.driving(request.startLng(), request.startLat(), request.endLng(), request.endLat())));
    }

    public TmapRouteResDTO walking(TmapRouteReqDTO request) {
        return toRoute(call(() ->
                tmapClient.pedestrian(request.startLng(), request.startLat(), request.endLng(), request.endLat())));
    }

    // Tmap 응답 본문에는 내부 정보가 섞일 수 있어 로그로만 남기고, 클라이언트에는 우리 에러 코드만 내려준다.
    private com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO call(
            Supplier<com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO> tmapCall) {
        try {
            return tmapCall.get();
        } catch (HttpClientErrorException.BadRequest ex) {
            log.warn("[Tmap] 경로 탐색 불가 요청: {}", ex.getResponseBodyAsString());
            throw new BusinessException(TmapRouteErrorCode.ROUTE_NOT_AVAILABLE);
        } catch (RestClientResponseException ex) {
            log.error("[Tmap] 호출 실패 status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new BusinessException(TmapRouteErrorCode.ROUTE_API_FAILED);
        } catch (ResourceAccessException ex) {
            log.error("[Tmap] 연결 실패 또는 응답 지연", ex);
            throw new BusinessException(TmapRouteErrorCode.ROUTE_API_TIMEOUT);
        } catch (RestClientException ex) {
            log.error("[Tmap] 응답 처리 실패", ex);
            throw new BusinessException(TmapRouteErrorCode.ROUTE_API_FAILED);
        }
    }

    private TmapRouteResDTO toRoute(
            com.all4land.parkinglotnavigator.domain.external.tmap.dto.TmapRouteResDTO response) {
        List<FeatureDTO> features = response == null || response.features() == null ? List.of() : response.features();
        FeaturePropertiesDTO summary = features.stream()
                .map(FeatureDTO::properties)
                .filter(p -> p != null && p.totalDistance() != null && p.totalTime() != null)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[Tmap] 응답에 경로 요약(totalDistance, totalTime)이 없습니다.");
                    return new BusinessException(TmapRouteErrorCode.ROUTE_API_FAILED);
                });
        return new TmapRouteResDTO(summary.totalDistance(), summary.totalTime(), lineCoordinates(features));
    }

    // 구간(LineString)별 좌표를 순서대로 이어 붙인다. 구간 경계 좌표가 겹칠 수 있어 직전 좌표와 같으면 건너뛴다.
    private List<double[]> lineCoordinates(List<FeatureDTO> features) {
        List<double[]> path = new ArrayList<>();
        for (FeatureDTO feature : features) {
            GeometryDTO geometry = feature.geometry();
            if (geometry == null || !LINE_STRING.equals(geometry.type()) || geometry.coordinates() == null) {
                continue;
            }
            for (JsonNode point : geometry.coordinates()) {
                double[] lngLat = {point.get(0).asDouble(), point.get(1).asDouble()};
                if (path.isEmpty() || !Arrays.equals(path.getLast(), lngLat)) {
                    path.add(lngLat);
                }
            }
        }
        return path;
    }
}
