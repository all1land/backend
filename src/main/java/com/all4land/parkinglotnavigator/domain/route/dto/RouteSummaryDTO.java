package com.all4land.parkinglotnavigator.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "통합 경로의 전체 및 이동 단계별 소요 시간(초)")
public record RouteSummaryDTO(
        @Schema(description = "전체 소요 시간(초)", example = "1507")
        long totalDurationSeconds,

        @Schema(description = "출발지에서 차량 출발 지점까지의 도보 시간(초)", example = "130")
        long departureWalkingDurationSeconds,

        @Schema(description = "차량 출발 지점에서 주차장 마커까지의 차량 시간(초)", example = "644")
        long drivingDurationSeconds,

        @Schema(description = "주차장 마커에서 최종 목적지까지의 도보 시간(초)", example = "733")
        long arrivalWalkingDurationSeconds) {

    public RouteSummaryDTO {
        if (totalDurationSeconds < 0
                || departureWalkingDurationSeconds < 0
                || drivingDurationSeconds < 0
                || arrivalWalkingDurationSeconds < 0) {
            throw new IllegalArgumentException("경로 시간은 음수일 수 없습니다.");
        }

        long sectionTotal = departureWalkingDurationSeconds + drivingDurationSeconds + arrivalWalkingDurationSeconds;

        if (totalDurationSeconds != sectionTotal) {
            throw new IllegalArgumentException("구간별 시간의 합계가 전체 시간과 일치해야 합니다.");
        }
    }

    public static RouteSummaryDTO empty() {
        return new RouteSummaryDTO(0, 0, 0, 0);
    }
}
