package com.all4land.parkinglotnavigator;

import com.all4land.parkinglotnavigator.global.config.ClockConfig;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingLotNavigatorApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ClockConfig.KST));
        SpringApplication.run(ParkingLotNavigatorApplication.class, args);
    }
}
