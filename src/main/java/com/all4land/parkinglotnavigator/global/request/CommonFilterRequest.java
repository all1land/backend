package com.all4land.parkinglotnavigator.global.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/** 공통 목록 검색 조건과 페이지 요청. 도메인 요청 DTO에서 상속한다. */
@Getter
public class CommonFilterRequest extends CommonPaginationRequest {

    @Schema(description = "검색어", example = "마곡")
    private String filterKeyword = "";

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword == null ? "" : filterKeyword.strip();
    }
}
