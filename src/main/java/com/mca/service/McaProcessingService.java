package com.mca.service;

import com.mca.client.TargetSystemClient;
import com.mca.fld.FldMessageBuilder;
import com.mca.fld.model.FldMessage;
import com.mca.model.TargetSystemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MCA 로그 처리 서비스 (간소화 버전)
 * - MCA 파싱 → FLD 빌드 → 타겟 전송
 * - 공백 포함 데이터를 그대로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McaProcessingService {

    private final FldMessageBuilder builder;
    private final TargetSystemClient client;

    /**
     * MCA 로그를 처리하고 타겟 시스템으로 전송
     *
     * @param mcaLog MCA 로그 원본
     * @return 타겟 시스템 응답
     */
    public TargetSystemResponse process(String mcaLog) {
        log.info("MCA 로그 처리 시작: {} bytes", mcaLog.length());

        try {
            // 1. MCA 파싱 → FLD 메시지 빌드 (공백 포함 그대로)
            FldMessage fldMessage = builder.buildMessage(mcaLog);
            log.debug("FLD 메시지 빌드 완료: {} bytes", fldMessage.totalLength());

            // 2. 타겟 시스템으로 전송
            TargetSystemResponse response = client.send(fldMessage);
            log.info("타겟 시스템 전송 완료: success={}", response.success());

            return response;

        } catch (Exception e) {
            log.error("MCA 로그 처리 실패", e);
            return TargetSystemResponse.failure("처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 재시도를 포함한 처리
     *
     * @param mcaLog MCA 로그 원본
     * @return 타겟 시스템 응답
     */
    public TargetSystemResponse processWithRetry(String mcaLog) {
        log.info("MCA 로그 처리 시작 (재시도 포함): {} bytes", mcaLog.length());

        try {
            // 1. MCA 파싱 → FLD 메시지 빌드
            FldMessage fldMessage = builder.buildMessage(mcaLog);
            log.debug("FLD 메시지 빌드 완료: {} bytes", fldMessage.totalLength());

            // 2. 재시도를 포함한 타겟 시스템 전송
            return client.sendWithRetry(fldMessage);

        } catch (Exception e) {
            log.error("MCA 로그 처리 실패", e);
            return TargetSystemResponse.failure("처리 중 오류 발생: " + e.getMessage());
        }
    }
}
