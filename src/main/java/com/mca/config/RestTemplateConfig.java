package com.mca.config;

import com.mca.client.config.TargetSystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final TargetSystemConfig targetSystemConfig;

    /**
     * 기본 RestTemplate 빈 (운영 및 개발 환경)
     */
    @Bean
    @Profile("!local")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(targetSystemConfig.getTimeout()))
            .setReadTimeout(Duration.ofMillis(targetSystemConfig.getTimeout()))
            .build();
    }
}
