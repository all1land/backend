package com.all4land.parkinglotnavigator.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "좌표 기반 통합 경로 응답")
public record RouteResDTO(
        @Schema(description = "지도에 그릴 순서가 지정된 경로 구간") List<RouteSegmentDTO> segments,

        @Schema(description = "전체 이동 거리(m)", example = "4008")
        long totalDistanceMeters,

        @Schema(description = "전체 소요 시간(초)", example = "1507")
        long totalDurationSeconds,

        @Schema(description = "전체 및 이동 단계별 시간 요약") RouteSummaryDTO summary) {

    /**
     * 기존 내부 호출과 테스트의 소스 호환성을 유지한다.
     * 구간 구분 정보가 없는 호출에서는 전체 시간을 차량 시간으로 간주한다.
     */
    public RouteResDTO(List<RouteSegmentDTO> segments, long totalDistanceMeters, long totalDurationSeconds) {
        this(
                segments,
                totalDistanceMeters,
                totalDurationSeconds,
                new RouteSummaryDTO(totalDurationSeconds, 0, totalDurationSeconds, 0));
    }
}
