package com.all4land.parkinglotnavigator.walkingroute;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param walkingSpeedMps 구간 소요 시간 계산에 쓰는 보행 속도(m/s)
 * @param maxSnapDistanceMeters 출발지/도착지에서 보행 링크까지 허용하는 최대 거리(m). 넘으면 좌표 오류로 보고 경로를 만들지 않는다.
 */
@ConfigurationProperties(prefix = "app.walking-route")
public record WalkingRouteProperties(double walkingSpeedMps, double maxSnapDistanceMeters) {}
