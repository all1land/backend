package com.all4land.parkinglotnavigator.domain.commoncode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "공통코드 등록 요청. parentId 생략 시 root 코드를 등록한다.")
public record CommonCodeCreateReqDTO(
        @Positive Long parentId,

        @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Z0-9_]+", message = "영문 대문자, 숫자, 밑줄만 허용합니다.") String code,

        @NotBlank @Size(max = 100) String name,
        @Min(0) Integer sortOrder,
        @Size(max = 500) String description,

        @Pattern(regexp = "ACTIVE|INACTIVE", message = "ACTIVE 또는 INACTIVE만 허용합니다.") String state) {}
