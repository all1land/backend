package com.all4land.parkinglotnavigator.domain.parkinglot.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.all4land.parkinglotnavigator.domain.parkinglot.repository.NearbyParkingLotProjection;
import com.all4land.parkinglotnavigator.domain.parkinglot.repository.ParkingLotDetailProjection;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ParkingLotResponseMappingTest {

    @Test
    void nearbyResponseIncludesSourceAndDisplayFields() {
        NearbyParkingLotProjection row = mock(NearbyParkingLotProjection.class);
        when(row.getId()).thenReturn(1L);
        when(row.getName()).thenReturn("신방화역");
        when(row.getRoadNameAddress()).thenReturn(null);
        when(row.getLotNumberAddress()).thenReturn("서울특별시 강서구 마곡동 739");
        when(row.getDisplayAddress()).thenReturn("서울특별시 강서구 마곡동 739");
        when(row.getCategoryCode()).thenReturn("PUBLIC");
        when(row.getCategoryName()).thenReturn("공영");
        when(row.getFeeInfo()).thenReturn("유료");
        when(row.getBaseFee()).thenReturn(290);
        when(row.getProviderCode()).thenReturn("B553774");
        when(row.getDataReferenceDate()).thenReturn(LocalDate.of(2026, 5, 15));

        ParkingLotNearbyResDTO response = ParkingLotNearbyResDTO.from(row);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("신방화역");
        assertThat(response.roadNameAddress()).isNull();
        assertThat(response.displayAddress()).isEqualTo("서울특별시 강서구 마곡동 739");
        assertThat(response.categoryName()).isEqualTo("공영");
        assertThat(response.feeInfo()).isEqualTo("유료");
        assertThat(response.baseFee()).isEqualTo(290);
        assertThat(response.providerCode()).isEqualTo("B553774");
        assertThat(response.dataReferenceDate()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    void detailResponseIncludesPreviouslyMissingFields() {
        ParkingLotDetailProjection row = mock(ParkingLotDetailProjection.class);
        when(row.getId()).thenReturn(2L);
        when(row.getExternalId()).thenReturn("115-2-000010");
        when(row.getDisplayAddress()).thenReturn("서울특별시 강서구 마곡동 739");
        when(row.getProviderName()).thenReturn("서울시설공단");
        when(row.getDataReferenceDate()).thenReturn(LocalDate.of(2026, 5, 15));

        when(row.getEntranceLatitude()).thenReturn(null);
        when(row.getEntranceLongitude()).thenReturn(null);

        ParkingLotDetailResDTO response = ParkingLotDetailResDTO.from(row);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.externalId()).isEqualTo("115-2-000010");
        assertThat(response.displayAddress()).isEqualTo("서울특별시 강서구 마곡동 739");
        assertThat(response.providerName()).isEqualTo("서울시설공단");
        assertThat(response.dataReferenceDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(response.entranceLatitude()).isNull();
        assertThat(response.entranceLongitude()).isNull();
    }

    @Test
    void nearbyResponseOldConstructorRemainsCompatible() {
        ParkingLotNearbyResDTO oldFormat = new ParkingLotNearbyResDTO(
                1L, "주차장", null, 10, 37.5, 126.9, 100.0, null, null, "PUBLIC", "공영", "OFF_STREET", "노외");

        assertThat(oldFormat.categoryName()).isEqualTo("공영");
        assertThat(oldFormat.displayAddress()).isNull();
        assertThat(oldFormat.providerCode()).isNull();
    }
}
