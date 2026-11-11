package com.all4land.parkinglotnavigator.global;

import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import com.all4land.parkinglotnavigator.swagger.HealthApi;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    @GetMapping("/health")
    public ApiResDTO<Map<String, String>> health() {
        return ApiResDTO.success(EnCmmScssCode.OK, Map.of("health", "UP"));
    }
}
