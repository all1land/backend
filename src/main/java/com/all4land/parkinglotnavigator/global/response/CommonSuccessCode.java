package com.all4land.parkinglotnavigator.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonSuccessCode implements BaseSuccessCode {
    OK(HttpStatus.OK, "200", "SUCCESS");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
