package com.all4land.parkinglotnavigator.external.tmap;

import com.all4land.parkinglotnavigator.external.tmap.dto.TmapPoiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

// 외부 호출만 담당한다. 응답 변환과 예외 처리는 PlaceService에서 한다.
@RequiredArgsConstructor
public class TmapPoiClient {

    private final RestClient restClient;

    public TmapPoiResponse search(String keyword, int page, int count) {
        var response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tmap/pois")
                        .queryParam("version", 1)
                        .queryParam("searchKeyword", "{keyword}")
                        .queryParam("searchType", "name")
                        // 서울 강서구 법정동 코드 11500: 시도 11 + 시군구 500
                        .queryParam("areaLLCode", "11")
                        .queryParam("areaLMCode", "500")
                        .queryParam("searchtypCd", "A")
                        .queryParam("reqCoordType", "WGS84GEO")
                        .queryParam("resCoordType", "WGS84GEO")
                        .queryParam("multiPoint", "Y")
                        .queryParam("poiGroupYn", "N")
                        .queryParam("page", page)
                        .queryParam("count", count)
                        .build(keyword))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(TmapPoiResponse.class);

        // 결과 없음(204)만 빈 검색 결과로 변환한다. 200의 빈 본문은 응답 오류로 처리한다.
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return new TmapPoiResponse(new TmapPoiResponse.SearchPoiInfo(0, null));
        }
        return response.getBody();
    }
}
