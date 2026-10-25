package com.all4land.parkinglotnavigator.drivingroute;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param maxSnapDistanceMeters 출발지/도착지에서 도로 링크까지 허용하는 최대 거리(m). 넘으면 좌표 오류로 보고 경로를 만들지 않는다.
 * @param snapCandidateCount 출발지/도착지마다 투영해 보는 가까운 링크 수. 링크는 방향별로 따로 있어서, 가장 가까운 링크 하나만 쓰면 진행 방향이 반대인
 *     링크에 붙어 돌아가는 경로가 나올 수 있다.
 * @param accessWalkingSpeedMps 접근 구간(원래 좌표 ↔ 도로 투영점) 소요 시간 계산에 쓰는 보행 속도(m/s)
 */
@ConfigurationProperties(prefix = "app.driving-route")
public record DrivingRouteProperties(
        double maxSnapDistanceMeters, int snapCandidateCount, double accessWalkingSpeedMps) {}
