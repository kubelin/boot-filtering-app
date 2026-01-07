package com.mca.fld.model;

import java.time.LocalDateTime;

/**
 * FLD 메시지 전체 구조
 */
public record FldMessage(
    FldHeader header,
    FldBody body,
    LocalDateTime createdAt
) {
    /**
     * FLD 고정길이 문자열로 변환
     */
    public String toFldString() {
        return header.toFldString() + body.toFldString();
    }

    /**
     * 전체 메시지 길이
     */
    public int totalLength() {
        return toFldString().length();
    }
}
