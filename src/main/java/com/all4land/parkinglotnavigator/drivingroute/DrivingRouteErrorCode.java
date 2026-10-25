package com.all4land.parkinglotnavigator.drivingroute;

import com.all4land.parkinglotnavigator.global.response.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DrivingRouteErrorCode implements BaseErrorCode {
    ROAD_LINK_NOT_NEARBY(HttpStatus.BAD_REQUEST, "DRIVING_ROUTE_400_001", "주변에 차량이 다닐 수 있는 도로가 없습니다."),
    DRIVING_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVING_ROUTE_404_001", "차량 경로를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
