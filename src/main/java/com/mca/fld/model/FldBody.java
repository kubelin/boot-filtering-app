package com.mca.fld.model;

import java.util.Map;

/**
 * FLD 바디 구조
 */
public record FldBody(
    String dataContent,              // 실제 데이터 내용
    int recordCount,                 // 레코드 개수
    Map<String, String> attributes   // 추가 속성
) {
    /**
     * FLD 고정길이 문자열로 변환 (구분자 제거)
     */
    public String toFixedLength() {
        if (dataContent == null || dataContent.isEmpty()) {
            return "";
        }
        // 구분자 제거
        return dataContent.replace("|", "").replace(",", "");
    }

    /**
     * 레코드 개수 포함 FLD 문자열
     */
    public String toFldString() {
        return String.format("%010d%s", recordCount, toFixedLength());
    }
}
