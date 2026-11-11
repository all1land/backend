package com.all4land.parkinglotnavigator.domain.commoncode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공통코드 응답")
public record CommonCodeResDTO(
        Long id,
        Long parentId,
        String code,
        String name,
        Integer level,
        Integer sortOrder,
        String description,
        String state,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
