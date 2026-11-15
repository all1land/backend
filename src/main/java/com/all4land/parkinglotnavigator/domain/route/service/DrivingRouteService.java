package com.all4land.parkinglotnavigator.domain.route.service;

import com.all4land.parkinglotnavigator.domain.route.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.domain.route.dto.DrivingRouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.DriveCost;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.PathPart;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.SnappedPoint;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnRouteErrCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@EnableConfigurationProperties(DrivingRouteProperties.class)
public class DrivingRouteService {

    // 반대 방향 차선 판단 기준: 차로 수 이상이면 중앙선을 넘어 들어갈 수 없다고 본다
    private static final int MULTI_LANE = 2;
    // 진행 방향이 이만큼 이상 차이 나면 반대 방향
    private static final double OPPOSITE_HEADING_DEGREES = 150;
    // 가장 가까운 링크보다 이만큼 이내로 더 먼 링크까지 반대 방향 차선으로 본다 (방향별 링크 간격 약 12m, 넓은 도로는 더 벌어짐)
    private static final double OPPOSITE_CARRIAGEWAY_GAP_METERS = 40;

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
     * <p>회전 제한(좌회전·직진·우회전 금지 등)을 지킨다. 출발·도착 후보 조합 모두 금지된 회전을 지나야만 갈 수 있으면, 경로를 못 찾았다고 하지 않고 그중
     * 가장 빠른 경로를 반환하며 경고 로그를 남긴다.
     *
     * <p>주행 시간은 제한속도에 속도 계수를 곱한 속도로 계산하고, 교차로 안 연결 링크를 지날 때마다 신호 대기 시간을 더한다(회전 방향에 따라 우회전 0번,
     * 직진 1번, 좌회전 2번). 두 값은 추정 보정값이며 실시간 교통 상황은 반영하지 않는다.
     *
     * @param startLat 출발지 위도 (WGS84, EPSG:4326)
     * @param startLng 출발지 경도 (WGS84, EPSG:4326)
     * @param endLat 도착지 위도 (WGS84, EPSG:4326)
     * @param endLng 도착지 경도 (WGS84, EPSG:4326)
     * @return 출발지부터 순서대로 이어지는 구간 목록. 모든 좌표는 WGS84(EPSG:4326)이며 coordinates는 [경도, 위도] 순서
     * @throws BusinessException 출발지/도착지 근처에 차량 도로가 없거나({@link EnRouteErrCode#ROAD_LINK_NOT_NEARBY}), 두
     *     지점이 네트워크로 이어지지 않을 때({@link EnRouteErrCode#DRIVING_ROUTE_NOT_FOUND})
     */
    public List<DrivingRouteSegmentDTO> route(double startLat, double startLng, double endLat, double endLng) {
        if (startLat == endLat && startLng == endLng) {
            return List.of();
        }

        List<SnappedPoint> starts = snap(startLat, startLng);
        List<SnappedPoint> ends = withoutOppositeCarriageway(snap(endLat, endLng));
        Choice best = choose(starts, ends);

        List<PathPart> path = drivingRouteRepository.findPath(best.start(), best.end());
        if (path.isEmpty() && !samePosition(best.start(), best.end())) {
            throw new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND);
        }

        List<Piece> pieces = new ArrayList<>();
        pieces.add(
                accessPiece(startLng, startLat, best.start().lng(), best.start().lat(), best.start()));
        path.forEach(part -> pieces.add(new Piece(part.coordinates(), part.distanceMeters(), part.durationSeconds())));
        pieces.add(accessPiece(best.end().lng(), best.end().lat(), endLng, endLat, best.end()));
        return toSegments(pieces);
    }

    private List<SnappedPoint> snap(double lat, double lng) {
        List<SnappedPoint> candidates = drivingRouteRepository.snap(
                lat, lng, properties.snapCandidateCount(), properties.maxSnapDistanceMeters());
        if (candidates.isEmpty()) {
            throw new BusinessException(EnRouteErrCode.ROAD_LINK_NOT_NEARBY);
        }
        return candidates;
    }

    // 도착 후보에서 차로가 여러 개인 도로의 반대 방향 차선을 뺀다.
    // 양방향 도로는 방향별 링크가 약 12m 떨어져 따로 있어서, 도착지 건너편 차선에 도착해 도로를 가로질러 들어가는 경로가 돌아오는 경로보다
    // 싸게 계산될 수 있다. 차로가 여러 개인 도로는 중앙선을 넘어 들어갈 수 없다고 보고, 도착지에서 가장 가까운 링크와 같은 도로의 반대 방향
    // 링크를 후보에서 뺀다. 데이터에 중앙분리대 정보가 없어 차로 수로 판단하며, 1차로 도로는 길을 건너 들어갈 수 있다고 본다.
    // 출발 쪽은 걸어서 차에 가는 구간이라 적용하지 않는다.
    private static List<SnappedPoint> withoutOppositeCarriageway(List<SnappedPoint> candidates) {
        SnappedPoint nearest = candidates.getFirst();
        if (nearest.lanes() == null || nearest.lanes() < MULTI_LANE) {
            return candidates;
        }
        return candidates.stream()
                .filter(candidate -> candidate == nearest || !isOppositeCarriageway(nearest, candidate))
                .toList();
    }

    // 같은 도로(도로명이 같거나 둘 다 없음)이고, 진행 방향이 반대이며, 방향별 링크 간격 정도로 떨어진 링크
    private static boolean isOppositeCarriageway(SnappedPoint near, SnappedPoint other) {
        return Objects.equals(near.roadName(), other.roadName())
                && headingDifference(near.azimuth(), other.azimuth()) >= OPPOSITE_HEADING_DEGREES
                && other.distanceMeters() - near.distanceMeters() <= OPPOSITE_CARRIAGEWAY_GAP_METERS;
    }

    // 두 진행 방향의 차이(0~180도). 방향을 모르면 0으로 본다
    private static double headingDifference(double a, double b) {
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return 0;
        }
        double diff = Math.abs(a - b) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    // 주행 시간 + 양 끝 접근 구간 도보 시간이 가장 작은 출발·도착 후보 조합을 고른다.
    // 회전 제한을 어기는 조합은 주행 시간에 큰 비용이 더해져 있어서, 지키는 조합이 하나라도 있으면 그쪽이 선택된다.
    private Choice choose(List<SnappedPoint> starts, List<SnappedPoint> ends) {
        Choice best = null;
        for (DriveCost cost : drivingRouteRepository.findDriveCosts(starts, ends)) {
            best = better(
                    best,
                    starts.get(cost.startIndex()),
                    ends.get(cost.endIndex()),
                    cost.seconds(),
                    cost.violatesTurnRestriction());
        }
        // 같은 위치에 투영된 두 후보 사이는 주행할 필요가 없다. pgRouting이 비용을 돌려주지 않을 수 있어 직접 넣는다.
        for (SnappedPoint start : starts) {
            for (SnappedPoint end : ends) {
                if (samePosition(start, end)) {
                    best = better(best, start, end, 0, false);
                }
            }
        }
        if (best == null) {
            throw new BusinessException(EnRouteErrCode.DRIVING_ROUTE_NOT_FOUND);
        }
        if (best.violatesTurnRestriction()) {
            log.warn(
                    "[DrivingRoute] 회전 제한을 지키는 경로가 없어 금지된 회전을 지나는 경로를 반환합니다. start link={}, end link={}",
                    best.start().linkId(),
                    best.end().linkId());
        }
        return best;
    }

    private Choice better(
            Choice current,
            SnappedPoint start,
            SnappedPoint end,
            double driveSeconds,
            boolean violatesTurnRestriction) {
        double accessSeconds = (start.distanceMeters() + end.distanceMeters()) / properties.accessWalkingSpeedMps();
        Choice candidate = new Choice(start, end, driveSeconds + accessSeconds, violatesTurnRestriction);
        return current == null || candidate.totalSeconds() < current.totalSeconds() ? candidate : current;
    }

    private static boolean samePosition(SnappedPoint a, SnappedPoint b) {
        return a.linkId() == b.linkId() && a.fraction() == b.fraction();
    }

    // 접근 구간: 원래 좌표와 도로 위 투영점을 잇는 직선 구간. 걸어서 이동한다고 본다.
    private Piece accessPiece(double fromLng, double fromLat, double toLng, double toLat, SnappedPoint snapped) {
        return new Piece(
                List.of(new double[] {fromLng, fromLat}, new double[] {toLng, toLat}),
                snapped.distanceMeters(),
                snapped.distanceMeters() / properties.accessWalkingSpeedMps());
    }

    // 구간마다 반올림하면 구간이 많을수록 합계 오차가 쌓인다(구간 약 40개에 최대 4초).
    // 처음부터 그 구간까지의 누적값을 반올림해 직전 누적값과의 차이를 구간 값으로 쓴다. 각 구간 값은 1 안쪽으로만 달라지고,
    // 구간을 모두 더하면 전체를 한 번에 반올림한 값과 같다. 누적값은 줄지 않아 구간 값이 음수가 되지 않는다.
    private static List<DrivingRouteSegmentDTO> toSegments(List<Piece> pieces) {
        List<DrivingRouteSegmentDTO> segments = new ArrayList<>();
        double distance = 0;
        double seconds = 0;
        long roundedDistance = 0;
        long roundedSeconds = 0;
        for (Piece piece : pieces) {
            distance += piece.distanceMeters();
            seconds += piece.seconds();
            long nextDistance = Math.round(distance);
            long nextSeconds = Math.round(seconds);
            segments.add(segment(
                    piece.coordinates(), (int) (nextDistance - roundedDistance), (int) (nextSeconds - roundedSeconds)));
            roundedDistance = nextDistance;
            roundedSeconds = nextSeconds;
        }
        return segments;
    }

    private static DrivingRouteSegmentDTO segment(List<double[]> coordinates, int distanceMeters, int seconds) {
        double[] first = coordinates.getFirst();
        double[] last = coordinates.getLast();
        return new DrivingRouteSegmentDTO(first[1], first[0], last[1], last[0], distanceMeters, seconds, coordinates);
    }

    /**
     * 반올림 전 구간.
     *
     * @param coordinates 구간 선형 좌표열. WGS84, [경도, 위도] 순서
     * @param distanceMeters 구간 거리(m)
     * @param seconds 구간 소요 시간(초)
     */
    private record Piece(List<double[]> coordinates, double distanceMeters, double seconds) {}

    /**
     * @param start 선택한 출발 투영점
     * @param end 선택한 도착 투영점
     * @param totalSeconds 주행 시간 + 양 끝 접근 구간 도보 시간(초)
     * @param violatesTurnRestriction 회전 제한을 어겨야만 갈 수 있는 조합인지
     */
    private record Choice(SnappedPoint start, SnappedPoint end, double totalSeconds, boolean violatesTurnRestriction) {}
}
