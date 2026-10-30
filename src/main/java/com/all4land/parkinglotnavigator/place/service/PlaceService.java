package com.all4land.parkinglotnavigator.place.service;

import com.all4land.parkinglotnavigator.external.tmap.TmapPoiClient;
import com.all4land.parkinglotnavigator.external.tmap.TmapPoiProperties;
import com.all4land.parkinglotnavigator.external.tmap.dto.TmapPoiResponse;
import com.all4land.parkinglotnavigator.external.tmap.dto.TmapPoiResponse.Poi;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.CommonErrorCode;
import com.all4land.parkinglotnavigator.place.PlaceErrorCode;
import com.all4land.parkinglotnavigator.place.dto.PlaceResponse;
import com.all4land.parkinglotnavigator.place.dto.PlaceSearchResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final int MAX_CANDIDATES = 100;

    private final TmapPoiClient tmapPoiClient;
    private final TmapPoiProperties properties;

    public PlaceSearchResponse search(String keyword, int page, int count) {
        String normalized = keyword == null ? "" : keyword.strip();
        if (normalized.isEmpty() || normalized.length() > 100 || page < 1 || page > 200 || count < 1 || count > 20) {
            throw new BusinessException(
                    CommonErrorCode.VALIDATION_ERROR, "검색어는 1~100자, page는 1~200, count는 1~20이어야 합니다.");
        }
        if (properties.appKey() == null || properties.appKey().isBlank()) {
            throw new BusinessException(PlaceErrorCode.PLACE_API_NOT_CONFIGURED);
        }

        // TMAP 한 번 호출로 가져온 후보를 정렬한 뒤 페이지를 나눈다.
        // count의 배수로 맞춰 후보 묶음 경계에서 페이지가 겹치거나 빠지지 않게 한다.
        int candidateCount = MAX_CANDIDATES / count * count;
        int startIndex = (page - 1) * count;
        int candidatePage = startIndex / candidateCount + 1;
        int offset = startIndex % candidateCount;
        TmapPoiResponse response = call(normalized, candidatePage, candidateCount);
        if (response == null
                || response.searchPoiInfo() == null
                || response.searchPoiInfo().totalCount() == null
                || response.searchPoiInfo().totalCount() < 0) {
            throw new BusinessException(PlaceErrorCode.PLACE_API_FAILED);
        }
        var info = response.searchPoiInfo();
        if (info.totalCount() == 0) {
            return new PlaceSearchResponse(page, 0, List.of());
        }
        if (info.pois() == null || info.pois().poi() == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_API_FAILED);
        }

        List<PlaceResponse> places = info.pois().poi().stream()
                .filter(Objects::nonNull)
                .filter(this::isSeoulGangseo)
                .map(this::toPlace)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(place -> matchRank(place.name(), normalized)))
                .skip(offset)
                .limit(count)
                .toList();
        return new PlaceSearchResponse(page, info.totalCount(), places);
    }

    // 같은 순위끼리는 TMAP의 정확도순을 유지한다.
    private int matchRank(String name, String keyword) {
        String normalizedName = name.strip().toLowerCase(Locale.ROOT);
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        if (normalizedName.equals(normalizedKeyword)) {
            return 0;
        }
        return normalizedName.startsWith(normalizedKeyword) ? 1 : 2;
    }

    private TmapPoiResponse call(String keyword, int page, int count) {
        try {
            return tmapPoiClient.search(keyword, page, count);
        } catch (RestClientResponseException ex) {
            // 검색어, appKey, 외부 응답 본문은 로그와 공통 오류 응답에 노출하지 않는다.
            log.warn("[Tmap POI] 호출 실패 status={}", ex.getStatusCode().value());
            throw new BusinessException(PlaceErrorCode.PLACE_API_FAILED);
        } catch (ResourceAccessException ex) {
            log.warn("[Tmap POI] 연결 실패 또는 응답 지연");
            throw new BusinessException(PlaceErrorCode.PLACE_API_TIMEOUT);
        } catch (RestClientException ex) {
            log.warn("[Tmap POI] 응답 처리 실패");
            throw new BusinessException(PlaceErrorCode.PLACE_API_FAILED);
        }
    }

    private boolean isSeoulGangseo(Poi poi) {
        return ("서울".equals(poi.upperAddrName()) || "서울특별시".equals(poi.upperAddrName()))
                && "강서구".equals(poi.middleAddrName());
    }

    private PlaceResponse toPlace(Poi poi) {
        double[] entrance = coordinates(poi.frontLat(), poi.frontLon());
        double[] center = coordinates(poi.noorLat(), poi.noorLon());
        double[] point = center != null ? center : entrance;
        if (point == null || poi.name() == null || poi.name().isBlank()) {
            return null;
        }
        return new PlaceResponse(
                poi.id(),
                poi.name(),
                roadAddress(poi),
                lotAddress(poi),
                point[0],
                point[1],
                entrance == null ? null : entrance[0],
                entrance == null ? null : entrance[1]);
    }

    // 좌표는 위도·경도 쌍으로 검증한다. 한쪽만 다른 좌표에서 가져오지 않는다.
    private double[] coordinates(String latitude, String longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        try {
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            if (!Double.isFinite(lat)
                    || !Double.isFinite(lng)
                    || lat < -90
                    || lat > 90
                    || lng < -180
                    || lng > 180
                    || lat == 0
                    || lng == 0) {
                return null;
            }
            return new double[] {lat, lng};
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String roadAddress(Poi poi) {
        if (poi.newAddressList() != null && poi.newAddressList().newAddress() != null) {
            String fullAddress = poi.newAddressList().newAddress().stream()
                    .filter(Objects::nonNull)
                    .map(TmapPoiResponse.NewAddress::fullAddressRoad)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse(null);
            if (fullAddress != null) {
                return fullAddress;
            }
        }
        if (poi.roadName() == null || poi.roadName().isBlank()) {
            return null;
        }
        return join(
                poi.upperAddrName(),
                poi.middleAddrName(),
                poi.roadName(),
                number(poi.firstBuildNo(), poi.secondBuildNo()));
    }

    private String lotAddress(Poi poi) {
        // TMAP mlClass: 1=대지, 2=산. 없는 부번이나 0은 주소 뒤에 붙이지 않는다.
        String lotNumber = number(poi.firstNo(), poi.secondNo());
        if ("2".equals(poi.mlClass()) && !lotNumber.isEmpty()) {
            lotNumber = "산 " + lotNumber;
        }
        return join(poi.upperAddrName(), poi.middleAddrName(), poi.lowerAddrName(), lotNumber);
    }

    private String number(String first, String second) {
        if (first == null || first.isBlank() || "0".equals(first)) {
            return "";
        }
        return second == null || second.isBlank() || "0".equals(second) ? first : first + "-" + second;
    }

    private String join(String... parts) {
        return Arrays.stream(parts)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }
}
