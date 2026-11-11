package com.all4land.parkinglotnavigator.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EnRouteErrCode implements CmmErrCode {
    ROAD_LINK_NOT_NEARBY(HttpStatus.BAD_REQUEST, "DRIVING_ROUTE_400_001", "주변에 차량이 다닐 수 있는 도로가 없습니다."),
    DRIVING_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVING_ROUTE_404_001", "차량 경로를 찾을 수 없습니다."),
    WALK_LINK_NOT_NEARBY(HttpStatus.BAD_REQUEST, "WALKING_ROUTE_400_001", "주변에 보행 가능한 도로가 없습니다."),
    WALKING_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "WALKING_ROUTE_404_001", "도보 경로를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
