package com.mca.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 타겟 시스템 설정
 */
@Data
@Component
@ConfigurationProperties(prefix = "target.system")
public class TargetSystemConfig {

    /**
     * 타겟 시스템 엔드포인트
     */
    private String endpoint;

    /**
     * 연결 타임아웃 (밀리초)
     */
    private int timeout = 5000;

    /**
     * 최대 재시도 횟수
     */
    private int maxRetries = 3;
}
