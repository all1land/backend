package com.all4land.parkinglotnavigator.domain.route.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.all4land.parkinglotnavigator.domain.route.DrivingRouteProperties;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.DriveCost;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.PathPart;
import com.all4land.parkinglotnavigator.domain.route.repository.DrivingRouteRepository.SnappedPoint;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// 도착지 건너편 차선 후보를 언제 빼는지 저장소 없이 검증한다.
class DrivingRouteArrivalSideTest {

    private static final SnappedPoint START = new SnappedPoint(1L, 0.5, 37.55, 126.83, 5);

    private DrivingRouteRepository repository;
    private DrivingRouteService service;

    @BeforeEach
    void setUp() {
        repository = mock(DrivingRouteRepository.class);
        service = new DrivingRouteService(repository, new DrivingRouteProperties(200, 4, 1.1, 0.7, 20));
        given(repository.snap(eq(37.0), eq(127.0), anyInt(), anyDouble())).willReturn(List.of(START));
        given(repository.findPath(any(), any()))
                .willReturn(List.of(
                        new PathPart(100, 10, List.of(new double[] {126.83, 37.55}, new double[] {126.82, 37.56}))));
    }

    @Test
    void excludesOppositeCarriagewayOfMultiLaneRoad() {
        // 도착지 쪽 차선(동쪽 방향, 10m)과 건너편 차선(서쪽 방향, 22m). 건너편 차선이 주행 시간이 훨씬 짧다
        SnappedPoint nearSide = arrival(2L, 10, "공항대로", 2, 90);
        SnappedPoint farSide = arrival(3L, 22, "공항대로", 2, 270);
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(nearSide, farSide));
        given(repository.findDriveCosts(any(), eq(List.of(nearSide)))).willReturn(List.of(new DriveCost(0, 0, 300)));

        service.route(37.0, 127.0, 37.1, 127.1);

        // 건너편 차선은 후보에서 빠져서 도착지 쪽 차선으로 간다
        verify(repository).findDriveCosts(List.of(START), List.of(nearSide));
        verify(repository).findPath(START, nearSide);
    }

    @Test
    void keepsOppositeSideOfSingleLaneRoad() {
        // 1차로 도로는 길을 건너 들어갈 수 있다고 본다
        SnappedPoint nearSide = arrival(2L, 10, "화곡로58길", 1, 90);
        SnappedPoint farSide = arrival(3L, 22, "화곡로58길", 1, 270);
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(nearSide, farSide));
        given(repository.findDriveCosts(any(), any()))
                .willReturn(List.of(new DriveCost(0, 0, 300), new DriveCost(0, 1, 100)));

        service.route(37.0, 127.0, 37.1, 127.1);

        verify(repository).findPath(START, farSide);
    }

    @Test
    void keepsParallelLinkOfAnotherRoad() {
        // 도로명이 다르면 반대 방향이라도 다른 도로(옆길 등)로 본다
        SnappedPoint nearSide = arrival(2L, 10, "공항대로", 2, 90);
        SnappedPoint sideRoad = arrival(3L, 22, "공항대로36길", 1, 270);
        given(repository.snap(eq(37.1), eq(127.1), anyInt(), anyDouble())).willReturn(List.of(nearSide, sideRoad));
        given(repository.findDriveCosts(any(), any()))
                .willReturn(List.of(new DriveCost(0, 0, 300), new DriveCost(0, 1, 100)));

        service.route(37.0, 127.0, 37.1, 127.1);

        verify(repository).findPath(START, sideRoad);
    }

    private static SnappedPoint arrival(long linkId, double distance, String roadName, int lanes, double azimuth) {
        return new SnappedPoint(linkId, 0.5, 37.56, 126.82, distance, roadName, lanes, azimuth);
    }
}
