package com.mca.client;

import com.mca.client.config.TargetSystemConfig;
import com.mca.fld.model.FldMessage;
import com.mca.model.TargetSystemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 타겟 시스템 클라이언트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TargetSystemClient {

    private final RestTemplate restTemplate;
    private final TargetSystemConfig config;

    /**
     * FLD 메시지를 타겟 시스템으로 전송
     */
    public TargetSystemResponse send(FldMessage message) {
        log.info("타겟 시스템 호출 시작: {}", config.getEndpoint());

        try {
            // FLD 메시지를 문자열로 변환
            String fldString = message.toFldString();
            log.debug("전송 메시지: {} bytes", fldString.length());

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity<String> request = new HttpEntity<>(fldString, headers);

            // RestTemplate을 사용한 POST 요청
            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getEndpoint(),
                request,
                String.class
            );

            log.info("타겟 시스템 응답 성공: code={}", response.getStatusCode().value());

            return TargetSystemResponse.success(
                response.getStatusCode().value(),
                response.getBody() != null ? response.getBody() : "SUCCESS"
            );

        } catch (Exception e) {
            log.error("타겟 시스템 호출 실패", e);
            return TargetSystemResponse.failure(e.getMessage());
        }
    }

    /**
     * 재시도를 포함한 전송
     */
    public TargetSystemResponse sendWithRetry(FldMessage message) {
        int attempts = 0;
        TargetSystemResponse response = null;

        while (attempts <= config.getMaxRetries()) {
            attempts++;
            log.debug("전송 시도 {}/{}", attempts, config.getMaxRetries() + 1);

            response = send(message);

            if (response.success()) {
                return response;
            }

            if (attempts <= config.getMaxRetries()) {
                log.warn("재시도 대기 중... ({}/{})", attempts, config.getMaxRetries());
                try {
                    Thread.sleep(1000L * attempts); // 지수 백오프
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return response;
    }
}
