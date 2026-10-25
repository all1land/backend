package com.all4land.parkinglotnavigator.integratedroute.service;

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
