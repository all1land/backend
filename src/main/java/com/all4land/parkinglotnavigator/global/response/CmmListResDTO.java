package com.all4land.parkinglotnavigator.global.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "공통 목록 응답")
public record CmmListResDTO<T>(
        @Schema(description = "페이지 정보") CmmPaginationDTO pagination,
        @Schema(description = "실제로 적용한 검색 조건") CmmFilterDTO filter,

        @Schema(description = "현재 페이지의 항목 목록 (결과가 없으면 빈 배열)") @JsonProperty("list")
        List<T> items) {

    public CmmListResDTO {

        Objects.requireNonNull(pagination, "CommonPagination must not be null");
        Objects.requireNonNull(filter, "CommonFilter must not be null");

        items = List.copyOf(items);

        if (items.size() != pagination.pageCnt()) {
            throw new IllegalArgumentException("Items size does not match pagination page count");
        }
    }

    /** 필터링과 정렬이 완료된 전체 목록을 잘라 응답한다. */
    public static <T> CmmListResDTO<T> fromItems(List<T> items, int pageIndex, int pageSize, CmmFilterDTO filter) {
        Objects.requireNonNull(items, "Items must not be null");

        CmmPaginationDTO.of(pageIndex, pageSize, 0, items.size());

        long offset = (long) (pageIndex - 1) * pageSize;
        int start = (int) Math.min(offset, items.size());
        int end = (int) Math.min(offset + pageSize, items.size());

        return fromPage(items.subList(start, end), items.size(), pageIndex, pageSize, filter);
    }

    /** 저장소에서 이미 페이징한 목록을 다시 자르지 않고 감싼다. */
    public static <T> CmmListResDTO<T> fromPage(
            List<T> items, long totalCnt, int pageIndex, int pageSize, CmmFilterDTO filter) {
        Objects.requireNonNull(items, "Items must not be null");

        return new CmmListResDTO<>(CmmPaginationDTO.of(pageIndex, pageSize, items.size(), totalCnt), filter, items);
    }
}
