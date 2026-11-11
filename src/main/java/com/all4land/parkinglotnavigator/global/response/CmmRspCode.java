package com.all4land.parkinglotnavigator.global.response;

import org.springframework.http.HttpStatus;

public interface CmmRspCode {

    String getCode();

    String getMessage();

    HttpStatus getHttpStatus();
}
