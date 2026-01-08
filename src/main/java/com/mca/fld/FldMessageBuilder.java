package com.mca.fld;

import com.mca.fld.model.FldMessage;
import com.mca.parser.McaMessageParser;
import com.mca.parser.model.McaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FLD 메시지 빌더 (간소화 버전)
 * - 공백 포함 고정길이 데이터를 그대로 사용
 * - 불필요한 패딩, 변환 제거
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FldMessageBuilder {

    private final McaMessageParser parser;

    /**
     * MCA 로그로부터 FLD 메시지 생성
     *
     * @param rawLog MCA 로그 원본
     * @return FLD 메시지 (공백 포함 그대로)
     */
    public FldMessage buildMessage(String rawLog) {
        log.debug("FLD 메시지 빌드 시작: {} bytes", rawLog.length());

        try {
            // MCA 파싱
            McaMessage mcaMessage = parser.parse(rawLog);

            // 헤더부/데이터부 그대로 사용 (공백 포함)
            String headerPart = mcaMessage.header();
            String dataPart = mcaMessage.body();

            FldMessage fldMessage = new FldMessage(headerPart, dataPart);

            log.debug("FLD 메시지 빌드 완료: {} bytes", fldMessage.totalLength());
            return fldMessage;

        } catch (Exception e) {
            log.error("FLD 메시지 빌드 실패", e);
            throw new RuntimeException("FLD 메시지 빌드 중 오류 발생", e);
        }
    }

    /**
     * MCA 메시지로부터 FLD 메시지 생성
     *
     * @param mcaMessage 파싱된 MCA 메시지
     * @return FLD 메시지
     */
    public FldMessage buildMessage(McaMessage mcaMessage) {
        log.debug("FLD 메시지 빌드 시작 (McaMessage)");

        String headerPart = mcaMessage.header();
        String dataPart = mcaMessage.body();

        FldMessage fldMessage = new FldMessage(headerPart, dataPart);

        log.debug("FLD 메시지 빌드 완료: {} bytes", fldMessage.totalLength());
        return fldMessage;
    }
}
