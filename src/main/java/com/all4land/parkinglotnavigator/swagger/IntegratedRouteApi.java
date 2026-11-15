package com.all4land.parkinglotnavigator.swagger;

import com.all4land.parkinglotnavigator.domain.route.dto.RouteCrdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.RouteResDTO;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Integrated Route", description = "도보·차량·도보 경로를 결합한 좌표 기반 통합 경로 API")
public interface IntegratedRouteApi {

    @Operation(
            summary = "좌표 기반 통합 경로 조회",
            description = "출발지에서 출발 측 POI, 선택한 주차장을 거쳐 목적지까지의 통합 경로를 반환한다. "
                    + "segments는 지도 경로 표시용이며 summary는 전체·출발 도보·차량·도착 도보 시간을 초 단위로 제공한다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "정상 (COMMON_200_001)",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                {
                                  "success": true,
                                  "code": "COMMON_200_001",
                                  "message": "성공적으로 요청을 처리했습니다.",
                                  "data": {
                                    "segments": [
                                      {"sequence": 1, "mode": "WALKING", "distanceMeters": 94,
                                       "durationSeconds": 130, "coordinates": [[126.8377,37.5586],[126.8376,37.5585]]},
                                      {"sequence": 2, "mode": "DRIVING", "distanceMeters": 3008,
                                       "durationSeconds": 644, "coordinates": [[126.8376,37.5585],[126.8177876,37.5667867]]},
                                      {"sequence": 3, "mode": "WALKING", "distanceMeters": 906,
                                       "durationSeconds": 733, "coordinates": [[126.8177876,37.5667867],[126.8270,37.5673]]}
                                    ],
                                    "totalDistanceMeters": 4008,
                                    "totalDurationSeconds": 1507,
                                    "summary": {
                                      "totalDurationSeconds": 1507,
                                      "departureWalkingDurationSeconds": 130,
                                      "drivingDurationSeconds": 644,
                                      "arrivalWalkingDurationSeconds": 733
                                    }
                                  }
                                }
                                """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "좌표·주차장 ID 검증 실패 또는 경로 탐색 불가"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주차장을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ApiResDTO<RouteResDTO> routeByCoordinates(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                            {
                                              "origin": {"latitude": 37.5586, "longitude": 126.8377},
                                              "departurePoi": {"latitude": 37.5585, "longitude": 126.8376},
                                              "parkingId": 1,
                                              "destination": {"latitude": 37.5673, "longitude": 126.8270}
                                            }
                                            """)))
                    RouteCrdReqDTO request);
}
