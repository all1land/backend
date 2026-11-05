package com.all4land.parkinglotnavigator.global.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonPaginationRequest {

    @Schema(description = "현재 페이지 (1부터 시작)", defaultValue = "1")
    @Min(1) private int pageIndex = 1;

    @Schema(description = "페이지당 항목 수", defaultValue = "10")
    @Min(1) private int pageSize = 10;
}
