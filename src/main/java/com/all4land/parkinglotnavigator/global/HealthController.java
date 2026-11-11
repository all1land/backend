package com.all4land.parkinglotnavigator.global;

import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import com.all4land.parkinglotnavigator.swagger.HealthApi;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(EnCmmScssCode.OK, Map.of("health", "UP"));
    }
}
