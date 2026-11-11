package com.all4land.parkinglotnavigator.domain.route.service;

import static org.junit.jupiter.api.Assertions.*;

import com.all4land.parkinglotnavigator.domain.route.drivingroute.dto.DrivingRouteSegmentDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.TravelMode;
import com.all4land.parkinglotnavigator.domain.route.walkingroute.dto.WalkingRouteSegmentDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class RouteAssemblerDrivingTest {

    private final RouteAssembler assembler = new RouteAssembler();

    @Test
    void marksDepartureAccessAsWalkingAndParkingAccessAsDriving() {
        List<double[]> roadCoordinates = List.of(
                new double[] {126.8171, 37.5661}, new double[] {126.8175, 37.5664}, new double[] {126.8180, 37.5668});

        var converted = assembler.driving(List.of(
                drivingSegment(10, 9, new double[] {126.8170, 37.5660}, new double[] {126.8171, 37.5661}),
                new DrivingRouteSegmentDTO(37.5661, 126.8171, 37.5668, 126.8180, 100, 12, roadCoordinates),
                drivingSegment(20, 18, new double[] {126.8180, 37.5668}, new double[] {126.8181, 37.5669})));

        assertEquals(
                List.of(TravelMode.WALKING, TravelMode.DRIVING, TravelMode.DRIVING),
                converted.stream().map(segment -> segment.mode()).toList());

        assertEquals(
                List.of(10, 100, 20),
                converted.stream().map(segment -> segment.distanceMeters()).toList());

        assertEquals(
                List.of(9, 12, 18),
                converted.stream().map(segment -> segment.durationSeconds()).toList());

        assertEquals(roadCoordinates.size(), converted.get(1).coordinates().size());

        for (int index = 0; index < roadCoordinates.size(); index++) {
            assertArrayEquals(
                    roadCoordinates.get(index), converted.get(1).coordinates().get(index), 0.0);
        }
    }

    @Test
    void preservesZeroDistanceAccessSegments() {
        double[] start = {126.8170, 37.5660};
        double[] end = {126.8180, 37.5670};

        var converted = assembler.driving(List.of(
                drivingSegment(0, 0, start, start),
                drivingSegment(100, 12, start, end),
                drivingSegment(0, 0, end, end)));

        assertEquals(3, converted.size());
        assertEquals(TravelMode.WALKING, converted.getFirst().mode());
        assertEquals(TravelMode.DRIVING, converted.getLast().mode());
        assertEquals(0, converted.getFirst().distanceMeters());
        assertEquals(0, converted.getLast().durationSeconds());
    }

    @Test
    void acceptsEmptyDrivingRouteForSamePoint() {
        assertTrue(assembler.driving(List.of()).isEmpty());
    }

    @Test
    void marksParkingArrivalAsDrivingWithoutRoadTravel() {
        var converted = assembler.driving(List.of(
                drivingSegment(10, 9, new double[] {126.8170, 37.5660}, new double[] {126.8171, 37.5661}),
                drivingSegment(20, 18, new double[] {126.8171, 37.5661}, new double[] {126.8172, 37.5662})));

        assertEquals(2, converted.size());
        assertEquals(
                List.of(TravelMode.WALKING, TravelMode.DRIVING),
                converted.stream().map(segment -> segment.mode()).toList());
    }

    @Test
    void rejectsSingleSegmentThatViolatesDrivingServiceContract() {
        var segment = drivingSegment(10, 9, new double[] {126.8170, 37.5660}, new double[] {126.8171, 37.5661});

        assertThrows(IllegalArgumentException.class, () -> assembler.driving(List.of(segment)));
    }

    @Test
    void combinesWalkingDrivingAndWalkingInOrder() {
        double[] origin = {126.8170, 37.5660};
        double[] poi = {126.8171, 37.5661};
        double[] roadStart = {126.8172, 37.5662};
        double[] roadEnd = {126.8180, 37.5670};
        double[] parking = {126.8181, 37.5671};
        double[] destination = {126.8182, 37.5672};

        var beforeDriving = assembler.walking(List.of(walkingSegment(30, 27, origin, poi)));

        var driving = assembler.driving(List.of(
                drivingSegment(10, 9, poi, roadStart),
                drivingSegment(100, 12, roadStart, roadEnd),
                drivingSegment(20, 18, roadEnd, parking)));

        var afterDriving = assembler.walking(List.of(walkingSegment(40, 36, parking, destination)));

        var response = assembler.assemble(List.of(beforeDriving, driving, afterDriving));

        assertEquals(5, response.segments().size());
        assertEquals(200L, response.totalDistanceMeters());
        assertEquals(102L, response.totalDurationSeconds());

        assertEquals(
                List.of(1, 2, 3, 4, 5),
                response.segments().stream().map(segment -> segment.sequence()).toList());

        assertEquals(
                List.of(
                        TravelMode.WALKING,
                        TravelMode.WALKING,
                        TravelMode.DRIVING,
                        TravelMode.DRIVING,
                        TravelMode.WALKING),
                response.segments().stream().map(segment -> segment.mode()).toList());

        assertEquals(TravelMode.DRIVING, response.segments().get(3).mode());
        assertArrayEquals(parking, response.segments().get(3).coordinates().getLast(), 0.0);
        assertEquals(TravelMode.WALKING, response.segments().get(4).mode());
        assertArrayEquals(parking, response.segments().get(4).coordinates().getFirst(), 0.0);

        assertArrayEquals(origin, response.segments().getFirst().coordinates().getFirst(), 0.0);
        assertArrayEquals(
                destination, response.segments().getLast().coordinates().getLast(), 0.0);

        for (int index = 1; index < response.segments().size(); index++) {
            assertArrayEquals(
                    response.segments().get(index - 1).coordinates().getLast(),
                    response.segments().get(index).coordinates().getFirst(),
                    0.0);
        }
    }

    private static DrivingRouteSegmentDTO drivingSegment(
            int distanceMeters, int durationSeconds, double[] start, double[] end) {
        return new DrivingRouteSegmentDTO(
                start[1], start[0], end[1], end[0], distanceMeters, durationSeconds, List.of(start, end));
    }

    private static WalkingRouteSegmentDTO walkingSegment(
            int distanceMeters, int durationSeconds, double[] start, double[] end) {
        return new WalkingRouteSegmentDTO(
                start[1], start[0], end[1], end[0], distanceMeters, durationSeconds, List.of(start, end));
    }
}
