package com.all4land.parkinglotnavigator.route;

import com.all4land.parkinglotnavigator.global.response.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RouteErrorCode implements BaseErrorCode {
    ROUTE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ROUTE_400_001", "해당 좌표로는 경로를 탐색할 수 없습니다."),
    ROUTE_API_FAILED(HttpStatus.BAD_GATEWAY, "ROUTE_502_001", "경로 탐색 서비스 호출에 실패했습니다."),
    ROUTE_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "ROUTE_504_001", "경로 탐색 서비스가 응답하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
