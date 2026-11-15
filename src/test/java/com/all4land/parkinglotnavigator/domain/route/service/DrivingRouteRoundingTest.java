package com.all4land.parkinglotnavigator.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.all4land.parkinglotnavigator.domain.route.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.domain.route.dto.DrivingRouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.DriveCost;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.PathPart;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.SnappedPoint;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

// 구간별 거리·시간을 반올림할 때 오차가 쌓이지 않는지 저장소 없이 검증한다.
class DrivingRouteRoundingTest {

    private static final double WALKING_SPEED_MPS = 1.1;

    @Test
    void segmentTotalsEqualRoundedExactTotals() {
        DrivingRouteRepository repository = mock(DrivingRouteRepository.class);
        DrivingRouteService service =
                new DrivingRouteService(repository, new DrivingRouteProperties(200, 4, WALKING_SPEED_MPS, 0.7, 20));
        // 양 끝 접근 구간 0.4m, 주행 구간 10개는 각 10.4m·1.4초
        SnappedPoint start = new SnappedPoint(1L, 0.5, 37.55, 126.83, 0.4);
        SnappedPoint end = new SnappedPoint(2L, 0.5, 37.56, 126.82, 0.4);
        given(repository.snap(eq(37.0), eq(127.0), anyInt(), anyDouble())).willReturn(List.of(start));
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(end));
        given(repository.findDriveCosts(any(), any())).willReturn(List.of(new DriveCost(0, 0, 14)));
        given(repository.findPath(start, end))
                .willReturn(Collections.nCopies(
                        10,
                        new PathPart(10.4, 1.4, List.of(new double[] {126.83, 37.55}, new double[] {126.82, 37.56}))));

        List<DrivingRouteSegmentDTO> segments = service.route(37.0, 127.0, 37.1, 127.1);

        double accessSeconds = 0.4 / WALKING_SPEED_MPS;
        // 구간마다 반올림해 더하면 시간 0 + 10 × 1 + 0 = 10초, 거리 0 + 10 × 10 + 0 = 100m가 된다
        assertThat(segments.stream()
                        .mapToInt(DrivingRouteSegmentDTO::durationSeconds)
                        .sum())
                .isEqualTo(Math.round(accessSeconds * 2 + 14));
        assertThat(segments.stream()
                        .mapToInt(DrivingRouteSegmentDTO::distanceMeters)
                        .sum())
                .isEqualTo(Math.round(0.4 * 2 + 104));

        // 각 구간 값은 반올림 전 값과 1 안쪽으로만 다르고 음수가 아니다
        assertThat(segments.getFirst().durationSeconds()).isBetween(0, 1);
        assertThat(segments.getLast().durationSeconds()).isBetween(0, 1);
        assertThat(segments.subList(1, segments.size() - 1)).allSatisfy(segment -> {
            assertThat(segment.durationSeconds()).isBetween(1, 2);
            assertThat(segment.distanceMeters()).isBetween(10, 11);
        });
    }
}
