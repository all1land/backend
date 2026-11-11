package com.all4land.parkinglotnavigator.global.exception;

import com.all4land.parkinglotnavigator.global.response.CmmErrCode;
import java.util.Objects;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final CmmErrCode errorCode;

    public BusinessException(CmmErrCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(CmmErrCode errorCode, String detailMessage) {
        super(resolveMessage(errorCode, detailMessage));
        this.errorCode = errorCode;
    }

    private static String resolveMessage(CmmErrCode errorCode, String detailMessage) {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        return detailMessage == null || detailMessage.isBlank() ? errorCode.getMessage() : detailMessage;
    }
}
