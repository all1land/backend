package com.all4land.parkinglotnavigator.external.tmap;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmap")
public record TmapProperties(String baseUrl, String appKey, Duration connectTimeout, Duration readTimeout) {}
