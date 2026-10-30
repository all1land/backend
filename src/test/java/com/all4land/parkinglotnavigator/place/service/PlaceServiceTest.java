package com.all4land.parkinglotnavigator.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.all4land.parkinglotnavigator.external.tmap.TmapPoiClient;
import com.all4land.parkinglotnavigator.external.tmap.TmapPoiProperties;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.place.PlaceErrorCode;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class PlaceServiceTest {

    // TMAP을 실제 호출하지 않고 주소·좌표 변환을 검사하기 위한 응답
    private static final String POI = """
            {"id":"example-poi","name":"테스트 목적지","noorLat":"37.56","noorLon":"126.83",
             "frontLat":"37.561","frontLon":"126.831","upperAddrName":"서울","middleAddrName":"강서구",
             "lowerAddrName":"마곡동","mlClass":"1","firstNo":"123","secondNo":"0",
             "roadName":"테스트로","firstBuildNo":"10","secondBuildNo":"2",
             "newAddressList":{"newAddress":[{"fullAddressRoad":"서울 강서구 테스트로 10-2"}]},
             "unknownField":"ignored"}
            """;

    private MockRestServiceServer server;
    private TmapPoiClient client;
    private PlaceService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder =
                RestClient.builder().baseUrl("https://apis.openapi.sk.com").defaultHeader("appKey", "test-key");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TmapPoiClient(builder.build());
        service = new PlaceService(client, properties("test-key"));
    }

    @Test
    void searchesOnlySeoulGangseoAndEncodesKeywordOnce() {
        String keyword = "마곡 +&%{역}";
        server.expect(request -> {
                    assertThat(request.getURI().getPath()).isEqualTo("/tmap/pois");
                    var params = UriComponentsBuilder.fromUri(request.getURI())
                            .build()
                            .getQueryParams();
                    assertThat(URLDecoder.decode(params.getFirst("searchKeyword"), StandardCharsets.UTF_8))
                            .isEqualTo(keyword);
                    assertThat(params.getFirst("areaLLCode")).isEqualTo("11");
                    assertThat(params.getFirst("areaLMCode")).isEqualTo("500");
                    assertThat(params.getFirst("searchType")).isEqualTo("name");
                    assertThat(params.getFirst("resCoordType")).isEqualTo("WGS84GEO");
                    assertThat(params.getFirst("multiPoint")).isEqualTo("Y");
                    assertThat(params.getFirst("page")).isEqualTo("1");
                    assertThat(params.getFirst("count")).isEqualTo("100");
                    assertThat(params).doesNotContainKey("appKey");
                })
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("appKey", "test-key"))
                .andExpect(header("Accept", "application/json"))
                .andRespond(withSuccess(response(POI, 8), MediaType.APPLICATION_JSON));

        var result = service.search("  " + keyword + "  ", 1, 5);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(8);
        assertThat(result.places()).hasSize(1);
        var place = result.places().getFirst();
        assertThat(place.poiId()).isEqualTo("example-poi");
        assertThat(place.name()).isEqualTo("테스트 목적지");
        assertThat(place.latitude()).isEqualTo(37.56);
        assertThat(place.longitude()).isEqualTo(126.83);
        assertThat(place.entranceLatitude()).isEqualTo(37.561);
        assertThat(place.entranceLongitude()).isEqualTo(126.831);
        assertThat(place.roadNameAddress()).isEqualTo("서울 강서구 테스트로 10-2");
        assertThat(place.lotNumberAddress()).isEqualTo("서울 강서구 마곡동 123");
        server.verify();
    }

    @Test
    void excludesOtherDistrictsAndBusanGangseo() {
        respond(response(POI + "," + POI.replace("강서구", "양천구") + "," + POI.replace("서울", "부산"), 3));
        assertThat(service.search("테스트", 1, 10).places()).hasSize(1);
    }

    @Test
    void usesEntireEntrancePairWhenCenterLatitudeIsInvalid() {
        respond(response(POI.replace("\"37.56\"", "\"NaN\""), 1));
        var place = service.search("테스트", 1, 10).places().getFirst();
        assertThat(place.latitude()).isEqualTo(37.561);
        assertThat(place.longitude()).isEqualTo(126.831);
    }

    @Test
    void excludesUnusableCoordinatesAndNamesWithoutInventingZeroCoordinates() {
        String invalidCoordinates = POI.replace("126.83", "999.83").replace("126.831", "999.831");
        String missingName = POI.replace("테스트 목적지", "");
        respond(response(POI + "," + invalidCoordinates + "," + missingName + ",null", 4));
        assertThat(service.search("테스트", 1, 10).places()).hasSize(1);
    }

    @Test
    void fallsBackToRoadFieldsAndKeepsMountainLotNumber() {
        String poi = POI.replace("\"fullAddressRoad\":\"서울 강서구 테스트로 10-2\"", "\"fullAddressRoad\":\"\"")
                .replace("\"mlClass\":\"1\"", "\"mlClass\":\"2\"")
                .replace("\"secondNo\":\"0\"", "\"secondNo\":\"4\"")
                .replace("\"frontLat\":\"37.561\"", "\"frontLat\":\"\"");
        respond(response(poi, 1));
        var place = service.search("테스트", 1, 10).places().getFirst();
        assertThat(place.roadNameAddress()).isEqualTo("서울 강서구 테스트로 10-2");
        assertThat(place.lotNumberAddress()).isEqualTo("서울 강서구 마곡동 산 123-4");
        assertThat(place.entranceLatitude()).isNull();
        assertThat(place.entranceLongitude()).isNull();
    }

    @Test
    void returnsEmptyListWhenNoResults() {
        respond("{\"searchPoiInfo\":{\"totalCount\":\"0\"}}");
        var result = service.search("검색결과없음", 1, 10);
        assertThat(result.totalCount()).isZero();
        assertThat(result.places()).isEmpty();
    }

    @Test
    void returnsEmptyListWhenProviderReturnsNoContent() {
        server.expect(request -> {}).andRespond(withStatus(HttpStatus.NO_CONTENT));

        var result = service.search("마곡", 1, 5);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalCount()).isZero();
        assertThat(result.places()).isEmpty();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "not-json"})
    void rejectsMalformedResponses(String body) {
        respond(body);
        assertError(PlaceErrorCode.PLACE_API_FAILED);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 429, 500, 503})
    void mapsProviderFailuresWithoutExposingBody(int status) {
        server.expect(request -> {})
                .andRespond(withStatus(HttpStatus.valueOf(status))
                        .body("secret provider detail")
                        .contentType(MediaType.TEXT_PLAIN));
        assertError(PlaceErrorCode.PLACE_API_FAILED);
    }

    @Test
    void mapsConnectionTimeout() {
        server.expect(request -> {}).andRespond(withException(new SocketTimeoutException("timed out")));
        assertError(PlaceErrorCode.PLACE_API_TIMEOUT);
    }

    @Test
    void missingKeyFailsWithoutCallingProvider() {
        service = new PlaceService(client, properties(""));
        assertError(PlaceErrorCode.PLACE_API_NOT_CONFIGURED);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void invalidKeywordDoesNotCallProvider(String keyword) {
        assertThatThrownBy(() -> service.search(keyword, 1, 10))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        ex -> assertThat(ex.getErrorCode().getCode()).isEqualTo("COMMON_400_002"));
        server.verify();
    }

    @Test
    void trimsKeywordBeforeSearching() {
        server.expect(request -> {
                    var params = UriComponentsBuilder.fromUri(request.getURI())
                            .build()
                            .getQueryParams();
                    assertThat(URLDecoder.decode(params.getFirst("searchKeyword"), StandardCharsets.UTF_8))
                            .isEqualTo("마곡");
                })
                .andRespond(withSuccess(response(POI, 1), MediaType.APPLICATION_JSON));

        assertThat(service.search("  마곡  ", 1, 5).places()).hasSize(1);
        server.verify();
    }

    private void assertError(PlaceErrorCode code) {
        assertThatThrownBy(() -> service.search("테스트", 1, 10)).isInstanceOfSatisfying(BusinessException.class, ex -> {
            assertThat(ex.getErrorCode()).isEqualTo(code);
            assertThat(ex.getMessage()).doesNotContain("secret provider detail");
        });
    }

    private void respond(String body) {
        server.expect(request -> {}).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private String response(String pois, int totalCount) {
        return "{\"searchPoiInfo\":{\"totalCount\":\"" + totalCount + "\",\"pois\":{\"poi\":[" + pois + "]}}}";
    }

    private TmapPoiProperties properties(String key) {
        return new TmapPoiProperties("https://apis.openapi.sk.com", key, Duration.ofSeconds(3), Duration.ofSeconds(5));
    }
}
