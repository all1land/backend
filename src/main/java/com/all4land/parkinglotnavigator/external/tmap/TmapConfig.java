package com.all4land.parkinglotnavigator.external.tmap;

import java.net.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(TmapProperties.class)
public class TmapConfig {

    @Bean
    public TmapClient tmapClient(TmapProperties properties) {
        // 키가 없어도 앱은 뜨게 두고 경로 탐색 호출만 실패시킨다. 테스트와 CI는 키 없이 실행되기 때문이다.
        String appKey = properties.appKey() == null ? "" : properties.appKey();
        if (appKey.isBlank()) {
            log.warn("tmap.app-key가 비어 있습니다. application-secret.yml 또는 TMAP_APP_KEY 환경변수를 설정하세요.");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("appKey", appKey)
                .build();
        return new TmapClient(restClient);
    }
}
