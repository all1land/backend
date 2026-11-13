package com.all4land.parkinglotnavigator.domain.place.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapperImpl;

class PlaceListReqDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void retainsDefaultPaginationAndKeyword() {
        var request = validRequest();
        assertThat(request.getPageIndex()).isEqualTo(1);
        assertThat(request.getPageSize()).isEqualTo(10);
        assertThat(request.getKeyword()).isEmpty();
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void bindsExistingQueryPropertiesAndNormalizesKeyword() {
        var request = validRequest();
        var binding = new BeanWrapperImpl(request);
        binding.setPropertyValue("keyword", "  마곡  ");
        binding.setPropertyValue("pageIndex", "2");
        binding.setPropertyValue("pageSize", "20");
        binding.setPropertyValue("centerLat", "37.56");
        binding.setPropertyValue("centerLon", "126.83");
        binding.setPropertyValue("zoom", "15");
        assertThat(request.getKeyword()).isEqualTo("마곡");
        assertThat(request.getPageIndex()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(20);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void enforcesPaginationLimits() {
        var request = validRequest();
        request.setPageIndex(200);
        request.setPageSize(20);
        assertThat(validator.validate(request)).isEmpty();
        request.setPageIndex(201);
        request.setPageSize(21);
        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("pageIndex", "pageSize");
        request.setPageIndex(0);
        request.setPageSize(0);
        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("pageIndex", "pageSize");
    }

    @Test
    void requiresBoundsAndRejectsReversedOrNonFiniteBounds() {
        assertThat(validator.validate(new PlaceListReqDTO()))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("minLat", "minLon", "maxLat", "maxLon");
        var request = validRequest();
        request.setMinLat(request.getMaxLat());
        assertThat(request.isValidBounds()).isFalse();
        assertThat(validator.validate(request)).isNotEmpty();
        request.setMinLat(Double.NaN);
        assertThat(request.isValidBounds()).isFalse();
    }

    @Test
    void validatesOptionalCenterPairAndZoom() {
        var request = validRequest();
        request.setCenterLat(37.56);
        assertThat(validator.validate(request)).isNotEmpty();
        request.setCenterLon(126.83);
        assertThat(validator.validate(request)).isEmpty();
        request.setCenterLon(Double.POSITIVE_INFINITY);
        assertThat(request.isValidCenter()).isFalse();
        request.setCenterLon(126.83);
        request.setZoom(-1);
        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("zoom");
    }

    @Test
    void retainsKeywordLengthAndNullHandling() {
        var request = validRequest();
        request.setKeyword("x".repeat(101));
        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("keyword");
        request.setKeyword(null);
        assertThat(request.getKeyword()).isEmpty();
        assertThat(validator.validate(request)).isEmpty();
    }

    private PlaceListReqDTO validRequest() {
        var request = new PlaceListReqDTO();
        request.setMinLat(37.55);
        request.setMinLon(126.82);
        request.setMaxLat(37.58);
        request.setMaxLon(126.85);
        return request;
    }
}
