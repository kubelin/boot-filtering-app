package com.mca.fld.model;

/**
 * FLD 메시지 (간소화 버전)
 * - 헤더부: 고정길이 필드들 (공백 포함)
 * - 데이터부: 가변길이 데이터 (공백 포함)
 * - 모든 공백은 데이터의 일부로 취급
 */
public record FldMessage(
    String header,    // 헤더부 (공백 포함 그대로)
    String data       // 데이터부 (공백 포함 그대로)
) {
    /**
     * FLD 고정길이 문자열로 변환
     * 구분자 없이 헤더+데이터 연결
     */
    public String toFldString() {
        if (data == null || data.isEmpty()) {
            return header;
        }
        return header + data;
    }

    /**
     * 구분자 포함 문자열 (디버깅용)
     */
    public String toDelimitedString() {
        if (data == null || data.isEmpty()) {
            return header;
        }
        return header + "|" + data;
    }

    /**
     * 전체 메시지 길이
     */
    public int totalLength() {
        return toFldString().length();
    }
}
