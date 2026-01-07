package com.mca.fld;

import com.mca.fld.model.FldBody;
import com.mca.fld.model.FldHeader;
import com.mca.fld.model.FldMessage;
import com.mca.parser.model.McaLogData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FLD 메시지 빌더
 */
@Slf4j
@Component
public class FldMessageBuilder {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * MCA 로그 데이터로부터 FLD 메시지 생성
     */
    public FldMessage buildMessage(McaLogData logData) {
        log.debug("FLD 메시지 빌드 시작");

        try {
            // 헤더 생성
            FldHeader header = buildHeader(logData);

            // 바디 생성
            FldBody body = buildBody(logData);

            FldMessage message = new FldMessage(header, body, LocalDateTime.now());

            log.debug("FLD 메시지 빌드 완료: {} bytes", message.totalLength());
            return message;

        } catch (Exception e) {
            log.error("FLD 메시지 빌드 실패", e);
            throw new RuntimeException("FLD 메시지 빌드 중 오류 발생", e);
        }
    }

    /**
     * FLD 헤더 생성
     */
    private FldHeader buildHeader(McaLogData logData) {
        String messageType = logData.getField("messageType");
        if (messageType == null) {
            messageType = "MCA0";
        }

        // 거래 ID 생성 (UUID 앞 20자리)
        String transactionId = UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 20);

        // 타임스탬프 생성
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 시스템 코드
        String systemCode = "MCASYS01";

        return new FldHeader(
            padRight(messageType, 4),
            padRight(transactionId, 20),
            padRight(timestamp, 14),
            padRight(systemCode, 8)
        );
    }

    /**
     * FLD 바디 생성
     */
    private FldBody buildBody(McaLogData logData) {
        String body = logData.getField("body");
        if (body == null) {
            body = "";
        }

        Map<String, String> attributes = new HashMap<>();
        attributes.put("source", "MCA_LOG");
        attributes.put("parseTime", LocalDateTime.now().toString());

        // 레코드 개수는 기본 1개
        int recordCount = 1;

        return new FldBody(body, recordCount, attributes);
    }

    /**
     * 문자열 우측 패딩
     */
    private String padRight(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return String.format("%-" + length + "s", value);
    }
}
