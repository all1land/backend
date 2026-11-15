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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// 회전 제한을 어기는 조합만 있을 때의 후보 선택을 저장소 없이 검증한다.
class DrivingRouteTurnRestrictionFallbackTest {

    private static final SnappedPoint START = new SnappedPoint(1L, 0.5, 37.55, 126.83, 5);
    private static final SnappedPoint START_FAR = new SnappedPoint(2L, 0.5, 37.55, 126.83, 50);
    private static final SnappedPoint END = new SnappedPoint(3L, 0.5, 37.56, 126.82, 5);

    private DrivingRouteRepository repository;
    private DrivingRouteService service;

    @BeforeEach
    void setUp() {
        repository = mock(DrivingRouteRepository.class);
        service = new DrivingRouteService(repository, new DrivingRouteProperties(200, 4, 1.1));
        given(repository.findPath(any(), any()))
                .willReturn(List.of(
                        new PathPart(100, 10, List.of(new double[] {126.83, 37.55}, new double[] {126.82, 37.56}))));
    }

    @Test
    void prefersCandidateThatKeepsTurnRestrictionsEvenIfFarther() {
        given(repository.snap(eq(37.0), eq(127.0), anyInt(), anyDouble())).willReturn(List.of(START, START_FAR));
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(END));
        given(repository.findDriveCosts(any(), any()))
                .willReturn(List.of(
                        new DriveCost(0, 0, DrivingRouteRepository.TURN_RESTRICTION_PENALTY_SECONDS + 10),
                        new DriveCost(1, 0, 300)));

        service.route(37.0, 127.0, 37.1, 127.1);

        // 가까운 후보(START)는 금지 회전을 지나야 해서, 접근 거리가 먼 START_FAR를 고른다
        org.mockito.Mockito.verify(repository).findPath(START_FAR, END);
    }

    @Test
    void returnsRouteEvenWhenEveryCandidateViolatesTurnRestriction() {
        given(repository.snap(eq(37.0), eq(127.0), anyInt(), anyDouble())).willReturn(List.of(START));
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(END));
        given(repository.findDriveCosts(any(), any()))
                .willReturn(List.of(new DriveCost(0, 0, DrivingRouteRepository.TURN_RESTRICTION_PENALTY_SECONDS + 10)));

        List<DrivingRouteSegmentDTO> segments = service.route(37.0, 127.0, 37.1, 127.1);

        // 경로를 못 찾았다고 하지 않고 접근 구간 + 주행 구간 + 접근 구간을 반환한다
        assertThat(segments).hasSize(3);
        assertThat(segments.get(1).durationSeconds()).isEqualTo(10);
    }
}
