package com.mca.parser.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCA 파싱 결과 메시지
 */
public record McaMessage(
    String header,                      // 헤더 문자열
    String body,                        // 바디 문자열
    Map<String, String> headerFields,   // 헤더 필드 맵
    List<String> bodyFields,            // 바디 필드 리스트
    Instant timestamp
) {
    /**
     * 전문 통신용 String 출력 (delimiter 제거, 공백 보존)
     * @return "헤더바디" (delimiter 없이 연결, 공백 그대로)
     */
    public String toFormatted() {
        if (body == null || body.isEmpty()) {
            return header;
        }
        return header + body;
    }

    /**
     * JSON 출력
     * @return JSON 문자열
     */
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();  // Java 8 Time 모듈 등록

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("header", headerFields);
            result.put("body", bodyFields);
            result.put("timestamp", timestamp.toString());

            return mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
