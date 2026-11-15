package com.all4land.parkinglotnavigator.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "최적 주차장 추천 응답")
public record ParkingRcmdResDTO(
        @Schema(description = "검색 반경 안에서 발견된 후보 수", example = "2")
        int candidateCount,

        @Schema(description = "경로 계산을 완료한 후보 수", example = "2")
        int evaluatedCount,

        @Schema(description = "전체 소요 시간이 짧은 순서의 추천 목록") List<ItemDTO> recommendations) {

    public record ItemDTO(
            @Schema(description = "추천 순위", example = "1") int rank,
            @Schema(description = "주차장 ID", example = "36") Long parkingId,

            @Schema(description = "주차장명", example = "등촌3동 구암길")
            String name,

            @Schema(description = "전체 이동 거리(m)", example = "1026")
            long totalDistanceMeters,

            @Schema(description = "전체 소요 시간(초)", example = "205")
            long totalDurationSeconds,

            @Schema(description = "해당 주차장을 이용하는 경로의 시간 요약") RouteSummaryDTO summary) {}
}
