package com.all4land.parkinglotnavigator.walkingroute;

import com.all4land.parkinglotnavigator.global.response.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WalkingRouteErrorCode implements BaseErrorCode {
    WALK_LINK_NOT_NEARBY(HttpStatus.BAD_REQUEST, "WALKING_ROUTE_400_001", "주변에 보행 가능한 도로가 없습니다."),
    WALKING_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "WALKING_ROUTE_404_001", "도보 경로를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
