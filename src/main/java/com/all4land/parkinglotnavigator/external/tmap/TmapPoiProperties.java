package com.all4land.parkinglotnavigator.external.tmap;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "tmap")
public record TmapPoiProperties(
        @DefaultValue("https://apis.openapi.sk.com") String baseUrl,
        @DefaultValue("") String appKey,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("5s") Duration readTimeout) {}
