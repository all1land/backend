package com.all4land.parkinglotnavigator.place;

import com.all4land.parkinglotnavigator.global.response.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PlaceErrorCode implements BaseErrorCode {
    PLACE_API_FAILED(HttpStatus.BAD_GATEWAY, "PLACE_502_001", "외부 장소 검색 서비스 호출 또는 응답 처리에 실패했습니다."),
    PLACE_API_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "PLACE_503_001", "장소 검색 서비스가 설정되지 않았습니다."),
    PLACE_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "PLACE_504_001", "외부 장소 검색 서비스에 연결하지 못했거나 응답이 지연되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
