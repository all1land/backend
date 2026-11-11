package com.all4land.parkinglotnavigator.global.response;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Schema(
        description = "도메인별 적용 검색 조건을 담는 JSON 객체",
        type = "object",
        additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
public record CmmFilterDTO(@JsonValue Map<String, Object> values) {

    public CmmFilterDTO {
        Objects.requireNonNull(values, "CmmFilterDTO values must not be null");
        values.keySet().forEach(key -> Objects.requireNonNull(key, "CmmFilterDTO keys must not be null"));
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static CmmFilterDTO empty() {
        return new CmmFilterDTO(Map.of());
    }
}
