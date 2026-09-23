package com.all4land.parkinglotnavigator;

import com.all4land.parkinglotnavigator.global.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ParkingLotNavigatorApplicationTests {

    @Test
    void contextLoads() {}
}
