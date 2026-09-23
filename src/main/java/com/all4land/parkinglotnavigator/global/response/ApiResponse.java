package com.all4land.parkinglotnavigator.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    @JsonIgnore
    private final HttpStatus httpStatus;

    public static <T> ApiResponse<T> success(BaseSuccessCode sc, T data) {
        return new ApiResponse<>(true, sc.getCode(), sc.getMessage(), data, sc.getHttpStatus());
    }

    public static ApiResponse<Void> success(BaseSuccessCode sc) {
        return new ApiResponse<>(true, sc.getCode(), sc.getMessage(), null, sc.getHttpStatus());
    }
}
