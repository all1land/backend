package com.all4land.parkinglotnavigator.global.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "목록 응답의 페이지 정보")
public record CmmPaginationDTO(
        @Schema(description = "현재 페이지 (1부터 시작)", example = "1") @JsonProperty("pageIndex")
        int pageIndex,

        @Schema(description = "페이지당 항목 수", example = "10") @JsonProperty("pageSize")
        int pageSize,

        @Schema(description = "전체 페이지 수 (전체 항목이 없으면 0)", example = "3") @JsonProperty("pageNum")
        long pageNum,

        @Schema(description = "현재 페이지의 실제 항목 수", example = "10") @JsonProperty("pageCnt")
        int pageCnt,

        @Schema(description = "전체 항목 수", example = "25") @JsonProperty("totalCnt")
        long totalCnt) {

    public CmmPaginationDTO {
        if (pageIndex < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page index and page size must be positive");
        }
        if (totalCnt < 0 || pageCnt < 0 || pageCnt > pageSize || pageCnt > totalCnt) {
            throw new IllegalArgumentException("Invalid page size or total count");
        }
        if (pageNum != totalPages(totalCnt, pageSize)) {
            throw new IllegalArgumentException("Page count does not match total count");
        }
    }

    public static CmmPaginationDTO of(int pageIndex, int pageSize, int pageCnt, long totalCnt) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        return new CmmPaginationDTO(pageIndex, pageSize, totalPages(totalCnt, pageSize), pageCnt, totalCnt);
    }

    private static long totalPages(long totalCnt, int pageSize) {
        return totalCnt / pageSize + (totalCnt % pageSize == 0 ? 0 : 1);
    }
}
