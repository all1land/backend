package com.all4land.parkinglotnavigator.parkinglot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.all4land.parkinglotnavigator.parkinglot.entity.ParkingLot;
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
}
