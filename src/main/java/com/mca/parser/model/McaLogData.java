package com.mca.parser.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 파싱된 MCA 로그 데이터
 */
public record McaLogData(
    String rawData,                  // 원본 데이터
    Map<String, String> fields,      // 파싱된 필드들
    LocalDateTime timestamp          // 파싱 시간
) {
    /**
     * 특정 필드 값 가져오기
     */
    public String getField(String fieldName) {
        return fields.get(fieldName);
    }

    /**
     * 필드 존재 여부 확인
     */
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }
}
