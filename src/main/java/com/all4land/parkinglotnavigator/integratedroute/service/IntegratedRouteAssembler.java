package com.all4land.parkinglotnavigator.integratedroute.service;

import com.all4land.parkinglotnavigator.drivingroute.dto.DrivingRouteSegment;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteResponse;
import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteSegment;
import com.all4land.parkinglotnavigator.integratedroute.dto.TravelMode;
import com.all4land.parkinglotnavigator.walkingroute.dto.WalkingRouteSegment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IntegratedRouteAssembler {

    public List<IntegratedRouteSegment> walking(List<WalkingRouteSegment> segments) {
        return segments.stream()
                .map(segment -> new IntegratedRouteSegment(
                        0,
                        TravelMode.WALKING,
                        segment.distanceMeters(),
                        segment.durationSeconds(),
                        segment.coordinates()))
                .toList();
    }

    /**
     * 차량 서비스 결과를 통합 구간으로 변환한다.
     * 첫 구간과 마지막 구간은 도보 접근 구간이며, 중간 구간은 차량 주행 구간이다.
     * 출발지와 도착지가 같은 경우 반환되는 빈 목록은 그대로 허용한다.
     */
    public List<IntegratedRouteSegment> driving(List<DrivingRouteSegment> segments) {
        if (segments.size() == 1) {
            throw new IllegalArgumentException("차량 경로는 빈 목록이거나 앞뒤 접근 구간을 포함해야 합니다.");
        }

        List<IntegratedRouteSegment> result = new ArrayList<>();

        for (int index = 0; index < segments.size(); index++) {
            DrivingRouteSegment segment = segments.get(index);
            boolean accessSegment = index == 0 || index == segments.size() - 1;

            result.add(new IntegratedRouteSegment(
                    0,
                    accessSegment ? TravelMode.WALKING : TravelMode.DRIVING,
                    segment.distanceMeters(),
                    segment.durationSeconds(),
                    segment.coordinates()));
        }

        return List.copyOf(result);
    }

    public IntegratedRouteResponse assemble(List<List<IntegratedRouteSegment>> sections) {
        List<IntegratedRouteSegment> result = new ArrayList<>();
        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;

        for (List<IntegratedRouteSegment> section : sections) {
            for (IntegratedRouteSegment segment : section) {
                result.add(new IntegratedRouteSegment(
                        result.size() + 1,
                        segment.mode(),
                        segment.distanceMeters(),
                        segment.durationSeconds(),
                        segment.coordinates()));

                totalDistanceMeters += segment.distanceMeters();
                totalDurationSeconds += segment.durationSeconds();
            }
        }

        return new IntegratedRouteResponse(List.copyOf(result), totalDistanceMeters, totalDurationSeconds);
    }
}
