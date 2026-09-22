package com.all4land.parkinglotnavigator;
	
import com.all4land.parkinglotnavigator.global.response.ApiResponse;
import com.all4land.parkinglotnavigator.global.response.ApiResponseBuilder;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@GetMapping("/health")
	public ResponseEntity<ApiResponse> health() {
		return ApiResponseBuilder.success(Map.of("health", "UP"));
	}
}