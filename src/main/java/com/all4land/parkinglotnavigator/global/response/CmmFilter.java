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
public record CmmFilter(@JsonValue Map<String, Object> values) {

    public CmmFilter {
        Objects.requireNonNull(values, "CmmFilter values must not be null");
        values.keySet().forEach(key -> Objects.requireNonNull(key, "CmmFilter keys must not be null"));
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static CmmFilter empty() {
        return new CmmFilter(Map.of());
    }
}
