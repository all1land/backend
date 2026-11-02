package com.all4land.parkinglotnavigator.integratedroute.service;

import static org.junit.jupiter.api.Assertions.*;

import com.all4land.parkinglotnavigator.integratedroute.dto.IntegratedRouteSegment;
import com.all4land.parkinglotnavigator.integratedroute.dto.TravelMode;
import com.all4land.parkinglotnavigator.walkingroute.dto.WalkingRouteSegment;
import java.util.List;
import org.junit.jupiter.api.Test;

class IntegratedRouteAssemblerTest {

    private final IntegratedRouteAssembler assembler = new IntegratedRouteAssembler();

    @Test
    void combinesSectionsInOrderAndPreservesGeometry() {
        List<double[]> walkingCoordinates = List.of(
                new double[] {126.8170, 37.5660}, new double[] {126.8171, 37.5662}, new double[] {126.8173, 37.5663});

        WalkingRouteSegment walking =
                new WalkingRouteSegment(37.5660, 126.8170, 37.5663, 126.8173, 50, 45, walkingCoordinates);

        IntegratedRouteSegment driving = new IntegratedRouteSegment(
                99, TravelMode.DRIVING, 1000, 120, List.of(new double[] {126.8173, 37.5663}, new double[] {
                    126.8270, 37.5670
                }));

        var response = assembler.assemble(List.of(assembler.walking(List.of(walking)), List.of(driving)));

        assertEquals(2, response.segments().size());
        assertEquals(1, response.segments().get(0).sequence());
        assertEquals(2, response.segments().get(1).sequence());
        assertEquals(TravelMode.WALKING, response.segments().get(0).mode());
        assertEquals(TravelMode.DRIVING, response.segments().get(1).mode());
        assertEquals(1050L, response.totalDistanceMeters());
        assertEquals(165L, response.totalDurationSeconds());

        assertEquals(3, response.segments().get(0).coordinates().size());
        assertArrayEquals(
                walkingCoordinates.get(1),
                response.segments().get(0).coordinates().get(1),
                0.0);
    }

    @Test
    void acceptsEmptyWalkingRouteForSamePoint() {
        var response = assembler.assemble(List.of(assembler.walking(List.of())));

        assertTrue(response.segments().isEmpty());
        assertEquals(0L, response.totalDistanceMeters());
        assertEquals(0L, response.totalDurationSeconds());
    }
}
