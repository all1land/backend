package com.all4land.parkinglotnavigator.domain.route.service;

import com.all4land.parkinglotnavigator.domain.route.dto.DrivingRouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteSummaryDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.TravelMode;
import com.all4land.parkinglotnavigator.domain.route.dto.WalkingRouteSegmentDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RouteAssembler {

    public List<RouteSegmentDTO> walking(List<WalkingRouteSegmentDTO> segments) {
        return segments.stream()
                .map(segment -> new RouteSegmentDTO(
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
    public List<RouteSegmentDTO> driving(List<DrivingRouteSegmentDTO> segments) {
        if (segments.size() == 1) {
            throw new IllegalArgumentException("차량 경로는 빈 목록이거나 앞뒤 접근 구간을 포함해야 합니다.");
        }

        List<RouteSegmentDTO> result = new ArrayList<>();

        for (int index = 0; index < segments.size(); index++) {
            DrivingRouteSegmentDTO segment = segments.get(index);
            boolean departureAccessSegment = index == 0;

            result.add(new RouteSegmentDTO(
                    0,
                    departureAccessSegment ? TravelMode.WALKING : TravelMode.DRIVING,
                    segment.distanceMeters(),
                    segment.durationSeconds(),
                    segment.coordinates()));
        }

        return List.copyOf(result);
    }

    public RouteResDTO assemble(List<List<RouteSegmentDTO>> sections) {
        List<RouteSegmentDTO> result = new ArrayList<>();
        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;

        for (List<RouteSegmentDTO> section : sections) {
            for (RouteSegmentDTO segment : section) {
                result.add(new RouteSegmentDTO(
                        result.size() + 1,
                        segment.mode(),
                        segment.distanceMeters(),
                        segment.durationSeconds(),
                        segment.coordinates()));

                totalDistanceMeters += segment.distanceMeters();
                totalDurationSeconds += segment.durationSeconds();
            }
        }

        RouteSummaryDTO summary = summarize(sections, result, totalDurationSeconds);

        return new RouteResDTO(List.copyOf(result), totalDistanceMeters, totalDurationSeconds, summary);
    }

    /**
     * 실제 통합 경로는 출발 도보, 차량, 도착 도보의 세 영역으로 전달된다.
     * 차량 영역의 첫 접근 구간은 WALKING이고 나머지는 DRIVING이다.
     */
    private static RouteSummaryDTO summarize(
            List<List<RouteSegmentDTO>> sections, List<RouteSegmentDTO> flattened, long totalDurationSeconds) {

        if (sections.size() == 3) {
            long departureWalking = duration(sections.get(0)) + duration(sections.get(1), TravelMode.WALKING);
            long driving = duration(sections.get(1), TravelMode.DRIVING);
            long arrivalWalking = duration(sections.get(2));

            return new RouteSummaryDTO(totalDurationSeconds, departureWalking, driving, arrivalWalking);
        }

        long departureWalking = 0;
        long driving = 0;
        long arrivalWalking = 0;
        boolean drivingStarted = false;

        for (RouteSegmentDTO segment : flattened) {
            if (segment.mode() == TravelMode.DRIVING) {
                drivingStarted = true;
                driving += segment.durationSeconds();
            } else if (drivingStarted) {
                arrivalWalking += segment.durationSeconds();
            } else {
                departureWalking += segment.durationSeconds();
            }
        }

        return new RouteSummaryDTO(totalDurationSeconds, departureWalking, driving, arrivalWalking);
    }

    private static long duration(List<RouteSegmentDTO> segments) {
        return segments.stream().mapToLong(RouteSegmentDTO::durationSeconds).sum();
    }

    private static long duration(List<RouteSegmentDTO> segments, TravelMode mode) {
        return segments.stream()
                .filter(segment -> segment.mode() == mode)
                .mapToLong(RouteSegmentDTO::durationSeconds)
                .sum();
    }
}
