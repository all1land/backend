package com.all4land.parkinglotnavigator.domain.route.service;

import com.all4land.parkinglotnavigator.domain.route.drivingroute.dto.DrivingRouteSegment;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResponse;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteSegment;
import com.all4land.parkinglotnavigator.domain.route.dto.TravelMode;
import com.all4land.parkinglotnavigator.domain.route.walkingroute.dto.WalkingRouteSegment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RouteAssembler {

    public List<RouteSegment> walking(List<WalkingRouteSegment> segments) {
        return segments.stream()
                .map(segment -> new RouteSegment(
                        0,
                        TravelMode.WALKING,
                        segment.distanceMeters(),
                        segment.durationSeconds(),
                        segment.coordinates()))
                .toList();
    }

    /**
     * 차량 서비스 결과를 통합 구간으로 변환한다.
     * 첫 구간은 출발지에서 도로까지의 도보 접근 구간이다.
     * 중간 차량 경로와 마지막 도로에서 주차장 마커까지의 접근 구간은 차량 구간으로 반환한다.
     * 출발지와 도착지가 같은 경우 반환되는 빈 목록은 그대로 허용한다.
     */
    public List<RouteSegment> driving(List<DrivingRouteSegment> segments) {
        if (segments.size() == 1) {
            throw new IllegalArgumentException("차량 경로는 빈 목록이거나 앞뒤 접근 구간을 포함해야 합니다.");
        }

        List<RouteSegment> result = new ArrayList<>();

        for (int index = 0; index < segments.size(); index++) {
            DrivingRouteSegment segment = segments.get(index);
            boolean departureAccessSegment = index == 0;

            result.add(new RouteSegment(
                    0,
                    departureAccessSegment ? TravelMode.WALKING : TravelMode.DRIVING,
                    segment.distanceMeters(),
                    segment.durationSeconds(),
                    segment.coordinates()));
        }

        return List.copyOf(result);
    }

    public RouteResponse assemble(List<List<RouteSegment>> sections) {
        List<RouteSegment> result = new ArrayList<>();
        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;

        for (List<RouteSegment> section : sections) {
            for (RouteSegment segment : section) {
                result.add(new RouteSegment(
                        result.size() + 1,
                        segment.mode(),
                        segment.distanceMeters(),
                        segment.durationSeconds(),
                        segment.coordinates()));

                totalDistanceMeters += segment.distanceMeters();
                totalDurationSeconds += segment.durationSeconds();
            }
        }

        return new RouteResponse(List.copyOf(result), totalDistanceMeters, totalDurationSeconds);
    }
}
