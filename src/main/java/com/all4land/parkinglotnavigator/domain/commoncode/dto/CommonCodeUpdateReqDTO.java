package com.all4land.parkinglotnavigator.domain.commoncode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "공통코드 수정 요청. 계층 이동(parentId 변경)은 지원하지 않는다.")
public record CommonCodeUpdateReqDTO(
        @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Z0-9_]+", message = "영문 대문자, 숫자, 밑줄만 허용합니다.") String code,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) Integer sortOrder,
        @Size(max = 500) String description,
        @NotNull @Pattern(regexp = "ACTIVE|INACTIVE", message = "ACTIVE 또는 INACTIVE만 허용합니다.") String state) {}
