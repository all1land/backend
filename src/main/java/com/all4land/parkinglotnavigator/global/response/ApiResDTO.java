package com.all4land.parkinglotnavigator.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResDTO<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> errorDetail;

    @JsonIgnore
    private final HttpStatus httpStatus;

    public static <T> ApiResDTO<T> success(CmmScssCode sc, T data) {
        return new ApiResDTO<>(true, sc.getCode(), sc.getMessage(), data, List.of(), sc.getHttpStatus());
    }

    public static ApiResDTO<Void> success(CmmScssCode sc) {
        return success(sc, null);
    }

    public static ApiResDTO<Void> fail(CmmErrCode ec) {
        return fail(ec, List.of());
    }

    public static ApiResDTO<Void> fail(CmmErrCode ec, List<String> errorDetail) {
        return new ApiResDTO<>(
                false, ec.getCode(), ec.getMessage(), null, List.copyOf(errorDetail), ec.getHttpStatus());
    }
}
