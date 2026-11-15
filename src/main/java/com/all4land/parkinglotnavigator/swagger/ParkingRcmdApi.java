package com.all4land.parkinglotnavigator.swagger;

import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdReqDTO;
import com.all4land.parkinglotnavigator.domain.route.dto.ParkingRcmdResDTO;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Parking Recommendation", description = "전체 이동 시간 기준 최적 주차장 추천 API")
public interface ParkingRcmdApi {

    @Operation(
            summary = "최적 주차장 추천",
            description =
                    "목적지 반경 안의 주차장을 평가하고 전체 소요 시간이 짧은 순서로 반환한다. " + "각 추천 결과의 summary에서 출발 도보·차량·도착 도보 시간을 확인할 수 있다.")
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
                                    "candidateCount": 2,
                                    "evaluatedCount": 2,
                                    "recommendations": [
                                      {
                                        "rank": 1,
                                        "parkingId": 36,
                                        "name": "등촌3동 구암길",
                                        "totalDistanceMeters": 1026,
                                        "totalDurationSeconds": 205,
                                        "summary": {
                                          "totalDurationSeconds": 205,
                                          "departureWalkingDurationSeconds": 20,
                                          "drivingDurationSeconds": 120,
                                          "arrivalWalkingDurationSeconds": 65
                                        }
                                      }
                                    ]
                                  }
                                }
                                """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "좌표·반경·추천 개수 검증 실패 또는 후보 경로 탐색 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ApiResDTO<ParkingRcmdResDTO> recommend(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                            {
                                              "origin": {"latitude": 37.5586, "longitude": 126.8377},
                                              "departurePoi": {"latitude": 37.5585, "longitude": 126.8376},
                                              "destination": {"latitude": 37.5673, "longitude": 126.8270},
                                              "radius": 2000,
                                              "limit": 3
                                            }
                                            """)))
                    ParkingRcmdReqDTO request);
}
