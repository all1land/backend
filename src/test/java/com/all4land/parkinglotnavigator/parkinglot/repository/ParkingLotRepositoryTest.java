package com.all4land.parkinglotnavigator.parkinglot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParkingLotRepositoryTest {

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @Test
    void getParkingLot37() {
        long count = parkingLotRepository.count();

        assertThat(count).isEqualTo(37);
    }

    @Test
    void nearbyProjectionIncludesParkingDetails() {
        var nearby = parkingLotRepository.findNearbyParkingLots(37.5667867, 126.8177876, 1000.0);

        assertThat(nearby).isNotEmpty();
        assertThat(nearby).anySatisfy(row -> {
            assertThat(row.getPhone()).isNotBlank();
            assertThat(row.getLotNumberAddress()).isNotBlank();
            assertThat(row.getCategoryCode()).isNotBlank();
            assertThat(row.getParkingTypeCode()).isNotBlank();
        });
        for (var row : nearby) {
            var entity = parkingLotRepository.findById(row.getId()).orElseThrow();
            assertThat(row.getPhone()).isEqualTo(entity.getPhone());
            assertThat(row.getLotNumberAddress()).isEqualTo(entity.getLotNumberAddress());
            assertThat(row.getCategoryCode()).isEqualTo(entity.getCategoryCode());
            assertThat(row.getParkingTypeCode()).isEqualTo(entity.getParkingTypeCode());
        }
    }
}
