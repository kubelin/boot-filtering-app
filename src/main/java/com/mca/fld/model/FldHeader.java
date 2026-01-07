package com.mca.fld.model;

import com.mca.model.FieldSpec;

import java.util.List;

/**
 * FLD 헤더 구조
 *
 * 메시지타입(4) + 거래ID(20) + 타임스탬프(14) + 시스템코드(8)
 */
public record FldHeader(
    String messageType,      // 4자리
    String transactionId,    // 20자리
    String timestamp,        // 14자리 (yyyyMMddHHmmss)
    String systemCode        // 8자리
) {
    // 헤더 필드 스펙
    private static final List<FieldSpec> SPECS = List.of(
        new FieldSpec("messageType", 4, FieldSpec.FieldType.STRING),
        new FieldSpec("transactionId", 20, FieldSpec.FieldType.STRING),
        new FieldSpec("timestamp", 14, FieldSpec.FieldType.STRING),
        new FieldSpec("systemCode", 8, FieldSpec.FieldType.STRING)
    );

    public static List<FieldSpec> specs() {
        return SPECS;
    }

    public static int totalLength() {
        return SPECS.stream().mapToInt(FieldSpec::length).sum();
    }

    /**
     * FLD 고정길이 문자열로 변환
     */
    public String toFldString() {
        return String.format("%-4s%-20s%-14s%-8s",
            messageType,
            transactionId,
            timestamp,
            systemCode
        );
    }
}
