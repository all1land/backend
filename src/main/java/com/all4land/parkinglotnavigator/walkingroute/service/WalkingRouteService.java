package com.all4land.parkinglotnavigator.walkingroute.service;

import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteErrorCode;
import com.all4land.parkinglotnavigator.walkingroute.WalkingRouteProperties;
import com.all4land.parkinglotnavigator.walkingroute.dto.WalkingRouteSegment;
import com.all4land.parkinglotnavigator.walkingroute.repository.WalkingRouteRepository;
import com.all4land.parkinglotnavigator.walkingroute.repository.WalkingRouteRepository.PathPart;
import com.all4land.parkinglotnavigator.walkingroute.repository.WalkingRouteRepository.SnappedPoint;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@EnableConfigurationProperties(WalkingRouteProperties.class)
public class WalkingRouteService {

    private final WalkingRouteRepository walkingRouteRepository;
    private final WalkingRouteProperties properties;

    /**
     * 두 지점 사이의 도보 경로를 구간 목록으로 계산한다.
     *
     * <p>출발지와 도착지를 각각 가장 가까운 보행 링크 위로 수선 투영한 뒤 그 사이 최단 경로를 찾는다. 원래 좌표와 투영점 사이는 직선 접근 구간으로 맨 앞과 맨
     * 뒤에 붙여서, 첫 구간은 출발지에서 시작하고 마지막 구간은 도착지에서 끝난다. 출발지와 도착지가 같으면 빈 목록을 반환한다.
     *
     * <p>두 지점이 가까워도 직선으로 안내하지 않는다. 건물 등 장애물 정보가 없어 직선이 걸을 수 있는 길인지 알 수 없기 때문이다.
     *
     * @param startLat 출발지 위도 (WGS84, EPSG:4326)
     * @param startLng 출발지 경도 (WGS84, EPSG:4326)
     * @param endLat 도착지 위도 (WGS84, EPSG:4326)
     * @param endLng 도착지 경도 (WGS84, EPSG:4326)
     * @return 출발지부터 순서대로 이어지는 구간 목록. 모든 좌표는 WGS84(EPSG:4326)이며 coordinates는 [경도, 위도] 순서
     * @throws BusinessException 출발지/도착지 근처에 보행 링크가 없거나({@link WalkingRouteErrorCode#WALK_LINK_NOT_NEARBY}), 두
     *     지점이 네트워크로 이어지지 않을 때({@link WalkingRouteErrorCode#WALKING_ROUTE_NOT_FOUND})
     */
    public List<WalkingRouteSegment> route(double startLat, double startLng, double endLat, double endLng) {
        if (startLat == endLat && startLng == endLng) {
            return List.of();
        }

        SnappedPoint start = snap(startLat, startLng);
        SnappedPoint end = snap(endLat, endLng);

        List<PathPart> path = walkingRouteRepository.findPath(start, end);
        boolean samePoint = start.linkId() == end.linkId() && start.fraction() == end.fraction();
        if (path.isEmpty() && !samePoint) {
            throw new BusinessException(WalkingRouteErrorCode.WALKING_ROUTE_NOT_FOUND);
        }

        List<WalkingRouteSegment> segments = new ArrayList<>();
        addAccessSegment(segments, startLng, startLat, start.lng(), start.lat(), start.distanceMeters());
        path.forEach(part -> segments.add(segment(part.coordinates(), part.distanceMeters())));
        addAccessSegment(segments, end.lng(), end.lat(), endLng, endLat, end.distanceMeters());
        return segments;
    }

    private SnappedPoint snap(double lat, double lng) {
        return walkingRouteRepository
                .snap(lat, lng)
                .filter(point -> point.distanceMeters() <= properties.maxSnapDistanceMeters())
                .orElseThrow(() -> new BusinessException(WalkingRouteErrorCode.WALK_LINK_NOT_NEARBY));
    }

    // 접근 구간: 원래 좌표와 보행 링크 위 투영점을 잇는 직선 구간. 원래 좌표가 이미 링크 위에 있으면 붙이지 않는다.
    private void addAccessSegment(
            List<WalkingRouteSegment> segments,
            double fromLng,
            double fromLat,
            double toLng,
            double toLat,
            double distanceMeters) {
        if (distanceMeters > 0) {
            segments.add(
                    segment(List.of(new double[] {fromLng, fromLat}, new double[] {toLng, toLat}), distanceMeters));
        }
    }

    private WalkingRouteSegment segment(List<double[]> coordinates, double distanceMeters) {
        double[] first = coordinates.getFirst();
        double[] last = coordinates.getLast();
        return new WalkingRouteSegment(
                first[1],
                first[0],
                last[1],
                last[0],
                (int) Math.round(distanceMeters),
                (int) Math.round(distanceMeters / properties.walkingSpeedMps()),
                coordinates);
    }
}
