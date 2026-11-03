package com.all4land.parkinglotnavigator.route.swagger;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.route.dto.RouteRequest;
import com.all4land.parkinglotnavigator.route.dto.RouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Route", description = "경로 안내 API (Tmap 연동)")
public interface RouteApi {

    @Operation(summary = "자동차 경로 안내", description = "출발지에서 도착지까지의 자동차 경로를 조회한다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 (COMMON_200_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "좌표 값 오류 (COMMON_400_002) 또는 경로 탐색 불가 (ROUTE_400_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "502",
                description = "Tmap 호출 실패 (ROUTE_502_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "504",
                description = "Tmap 응답 없음 (ROUTE_504_001)")
    })
    ApiResponse<RouteResponse> driving(@ParameterObject RouteRequest request);

    @Operation(summary = "보행자 경로 안내", description = "출발지에서 도착지까지의 보행자 경로를 조회한다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 (COMMON_200_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "좌표 값 오류 (COMMON_400_002) 또는 경로 탐색 불가 (ROUTE_400_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "502",
                description = "Tmap 호출 실패 (ROUTE_502_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "504",
                description = "Tmap 응답 없음 (ROUTE_504_001)")
    })
    ApiResponse<RouteResponse> walking(@ParameterObject RouteRequest request);
}
