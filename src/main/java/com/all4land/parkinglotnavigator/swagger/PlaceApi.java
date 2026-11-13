package com.all4land.parkinglotnavigator.swagger;

import com.all4land.parkinglotnavigator.domain.place.dto.PlaceListReqDTO;
import com.all4land.parkinglotnavigator.domain.place.dto.PlaceResDTO;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.CmmListResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Place", description = "DB 장소의 지도 영역 조회 API")
public interface PlaceApi {

    @Operation(
            summary = "지도 영역 안의 장소 목록 조회",
            description = "DB에 저장된 장소 중 minLat/minLon/maxLat/maxLon 경계 안에 있는 장소를 반환한다. "
                    + "좌표계는 WGS84이며 경계선 위 장소도 포함하고 삭제된 장소는 제외한다. "
                    + "minLat < maxLat, minLon < maxLon이어야 하며 날짜변경선을 가로지르는 영역은 지원하지 않는다. "
                    + "keyword는 선택이며 장소명 또는 도로명 주소에 포함된 문자열로 검색한다. "
                    + "centerLat/centerLon과 zoom은 선택 화면 정보이며 조회 범위나 정렬을 변경하지 않는다. "
                    + "DB id 오름차순으로 페이지를 나누고 totalCnt는 영역·검색 조건을 만족하는 전체 건수이다.",
            parameters = {
                @Parameter(
                        name = "centerLat",
                        description = "지도 중심 위도 (centerLon과 함께 전달)",
                        schema = @Schema(type = "number", minimum = "-90", maximum = "90", example = "37.56")),
                @Parameter(
                        name = "centerLon",
                        description = "지도 중심 경도 (centerLat과 함께 전달)",
                        schema = @Schema(type = "number", minimum = "-180", maximum = "180", example = "126.83"))
            })
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
                            "pagination": {"pageIndex": 1, "pageSize": 10, "pageNum": 1, "pageCnt": 1, "totalCnt": 1},
                            "filter": {"keyword": "마곡", "minLat": 37.55, "minLon": 126.82,
                                       "maxLat": 37.58, "maxLon": 126.85, "centerLat": 37.56, "centerLon": 126.83, "zoom": 15},
                            "list": [{"id": 1, "name": "마곡 목적지", "roadNameAddress": "서울 강서구 예시로 1",
                                      "lotNumberAddress": "서울 강서구 마곡동 1", "latitude": 37.56, "longitude": 126.83,
                                      "entranceLatitude": null, "entranceLongitude": null}]
                          }
                        }
                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "필수 경계 누락, 좌표 범위·경계 순서 오류, 페이지·검색어 오류 (COMMON_400_002)",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResDTO.class),
                                examples = @ExampleObject(value = """
                                {"success": false, "code": "COMMON_400_002", "message": "요청 값 검증에 실패했습니다.",
                                 "data": null, "errorDetail": ["minLat: 널이어서는 안됩니다"]}
                                """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "서버 오류 (COMMON_500_001)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResDTO.class)))
    })
    ApiResDTO<CmmListResDTO<PlaceResDTO>> getPlaces(@ParameterObject PlaceListReqDTO request);

    @Operation(
            summary = "장소 상세 조회",
            description = "DB 장소 ID로 삭제되지 않은 장소의 상세 정보를 조회한다. " + "id에는 장소 목록 응답의 poiId 값을 전달한다. 좌표계는 WGS84이다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 (COMMON_200_001)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "양수가 아니거나 숫자 형식이 아닌 ID (COMMON_400_002)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않거나 삭제된 장소 (COMMON_404_001)")
    })
    ApiResDTO<PlaceResDTO> getPlace(
            @Parameter(description = "장소 DB ID (목록 응답의 poiId)", required = true, example = "1") Long id);
}
