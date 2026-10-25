package com.all4land.parkinglotnavigator.drivingroute.service;

import com.all4land.parkinglotnavigator.drivingroute.DrivingRouteErrorCode;
import com.all4land.parkinglotnavigator.drivingroute.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.drivingroute.dto.DrivingRouteSegment;
import com.all4land.parkinglotnavigator.drivingroute.repository.DrivingRouteRepository;
import com.all4land.parkinglotnavigator.drivingroute.repository.DrivingRouteRepository.DriveCost;
import com.all4land.parkinglotnavigator.drivingroute.repository.DrivingRouteRepository.PathPart;
import com.all4land.parkinglotnavigator.drivingroute.repository.DrivingRouteRepository.SnappedPoint;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@EnableConfigurationProperties(DrivingRouteProperties.class)
public class DrivingRouteService {

    private final DrivingRouteRepository drivingRouteRepository;
    private final DrivingRouteProperties properties;

    /**
     * 두 지점 사이의 차량 경로를 구간 목록으로 계산한다.
     *
     * <p>출발지와 도착지를 각각 가까운 도로 링크 여러 개 위로 수선 투영하고, 출발·도착 후보 조합 중 주행 시간과 양 끝 접근 구간 도보 시간의 합이 가장 작은
     * 조합으로 최단 시간 경로를 찾는다. 링크는 방향별로 따로 있어서 가장 가까운 링크가 반대 방향이면 돌아가는 경로가 나오기 때문이다.
     *
     * <p>맨 앞과 맨 뒤 구간은 항상 접근 구간(원래 좌표 ↔ 도로 투영점을 잇는 직선)이고 걸어서 이동한다고 보고 소요 시간을 계산한다. 원래 좌표가 이미 도로 위에 있어
     * 거리가 0이어도 넣어서, 첫 구간과 마지막 구간이 항상 도보라는 것을 위치로 알 수 있게 한다. 그 사이 구간은 링크별 주행 구간이다. 출발지와 도착지가 같으면 빈 목록을
     * 반환한다.
     *
     * <p>주행 시간은 제한속도로 계속 달린다고 보고 계산해서 신호 대기와 교통 상황이 빠져 있다. 회전 제한(좌회전 금지 등)도 아직 반영하지 않는다.
     *
     * @param startLat 출발지 위도 (WGS84, EPSG:4326)
     * @param startLng 출발지 경도 (WGS84, EPSG:4326)
     * @param endLat 도착지 위도 (WGS84, EPSG:4326)
     * @param endLng 도착지 경도 (WGS84, EPSG:4326)
     * @return 출발지부터 순서대로 이어지는 구간 목록. 모든 좌표는 WGS84(EPSG:4326)이며 coordinates는 [경도, 위도] 순서
     * @throws BusinessException 출발지/도착지 근처에 차량 도로가 없거나({@link DrivingRouteErrorCode#ROAD_LINK_NOT_NEARBY}), 두
     *     지점이 네트워크로 이어지지 않을 때({@link DrivingRouteErrorCode#DRIVING_ROUTE_NOT_FOUND})
     */
    public List<DrivingRouteSegment> route(double startLat, double startLng, double endLat, double endLng) {
        if (startLat == endLat && startLng == endLng) {
            return List.of();
        }

        List<SnappedPoint> starts = snap(startLat, startLng);
        List<SnappedPoint> ends = snap(endLat, endLng);
        Choice best = choose(starts, ends);

        List<PathPart> path = drivingRouteRepository.findPath(best.start(), best.end());
        if (path.isEmpty() && !samePosition(best.start(), best.end())) {
            throw new BusinessException(DrivingRouteErrorCode.DRIVING_ROUTE_NOT_FOUND);
        }

        List<DrivingRouteSegment> segments = new ArrayList<>();
        segments.add(accessSegment(
                startLng, startLat, best.start().lng(), best.start().lat(), best.start()));
        path.forEach(part -> segments.add(segment(part.coordinates(), part.distanceMeters(), part.durationSeconds())));
        segments.add(accessSegment(best.end().lng(), best.end().lat(), endLng, endLat, best.end()));
        return segments;
    }

    private List<SnappedPoint> snap(double lat, double lng) {
        List<SnappedPoint> candidates = drivingRouteRepository.snap(
                lat, lng, properties.snapCandidateCount(), properties.maxSnapDistanceMeters());
        if (candidates.isEmpty()) {
            throw new BusinessException(DrivingRouteErrorCode.ROAD_LINK_NOT_NEARBY);
        }
        return candidates;
    }

    // 주행 시간 + 양 끝 접근 구간 도보 시간이 가장 작은 출발·도착 후보 조합을 고른다.
    private Choice choose(List<SnappedPoint> starts, List<SnappedPoint> ends) {
        Choice best = null;
        for (DriveCost cost : drivingRouteRepository.findDriveCosts(starts, ends)) {
            best = better(best, starts.get(cost.startIndex()), ends.get(cost.endIndex()), cost.seconds());
        }
        // 같은 위치에 투영된 두 후보 사이는 주행할 필요가 없다. pgRouting이 비용을 돌려주지 않을 수 있어 직접 넣는다.
        for (SnappedPoint start : starts) {
            for (SnappedPoint end : ends) {
                if (samePosition(start, end)) {
                    best = better(best, start, end, 0);
                }
            }
        }
        if (best == null) {
            throw new BusinessException(DrivingRouteErrorCode.DRIVING_ROUTE_NOT_FOUND);
        }
        return best;
    }

    private Choice better(Choice current, SnappedPoint start, SnappedPoint end, double driveSeconds) {
        double accessSeconds = (start.distanceMeters() + end.distanceMeters()) / properties.accessWalkingSpeedMps();
        Choice candidate = new Choice(start, end, driveSeconds + accessSeconds);
        return current == null || candidate.totalSeconds() < current.totalSeconds() ? candidate : current;
    }

    private static boolean samePosition(SnappedPoint a, SnappedPoint b) {
        return a.linkId() == b.linkId() && a.fraction() == b.fraction();
    }

    // 접근 구간: 원래 좌표와 도로 위 투영점을 잇는 직선 구간. 걸어서 이동한다고 본다.
    private DrivingRouteSegment accessSegment(
            double fromLng, double fromLat, double toLng, double toLat, SnappedPoint snapped) {
        return segment(
                List.of(new double[] {fromLng, fromLat}, new double[] {toLng, toLat}),
                snapped.distanceMeters(),
                snapped.distanceMeters() / properties.accessWalkingSpeedMps());
    }

    private static DrivingRouteSegment segment(List<double[]> coordinates, double distanceMeters, double seconds) {
        double[] first = coordinates.getFirst();
        double[] last = coordinates.getLast();
        return new DrivingRouteSegment(
                first[1],
                first[0],
                last[1],
                last[0],
                (int) Math.round(distanceMeters),
                (int) Math.round(seconds),
                coordinates);
    }

    /**
     * @param start 선택한 출발 투영점
     * @param end 선택한 도착 투영점
     * @param totalSeconds 주행 시간 + 양 끝 접근 구간 도보 시간(초)
     */
    private record Choice(SnappedPoint start, SnappedPoint end, double totalSeconds) {}
}
