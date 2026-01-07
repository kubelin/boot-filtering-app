package com.mca.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 로컬 테스트 환경 설정
 */
@Slf4j
@Configuration
@Profile("local")
public class LocalTestConfig {

    /**
     * Mock RestTemplate (로컬 테스트용)
     */
    @Bean
    public RestTemplate mockRestTemplate() {
        log.info("로컬 테스트 모드: Mock RestTemplate 활성화");

        return new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
                log.info("Mock 타겟 시스템 호출: {}", url);
                log.debug("요청 데이터: {}", request);

                // Mock 응답 생성
                String mockResponse = "MOCK_SUCCESS: 타겟 시스템 응답 시뮬레이션";

                @SuppressWarnings("unchecked")
                ResponseEntity<T> response = (ResponseEntity<T>) ResponseEntity
                    .status(HttpStatus.OK)
                    .body(mockResponse);

                return response;
            }
        };
    }
}
