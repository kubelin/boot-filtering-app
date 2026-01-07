package com.securities.parser.model;

import java.util.List;

/**
 * 증권 데이터 헤더 정의 (Java 17 record)
 * 
 * 예시: 메시지타입(4) + 거래일자(8) + 거래시간(6) + 전체건수(10)
 */
public record SecuritiesHeader(
    String messageType,
    String tradeDate,
    String tradeTime,
    Long totalCount
) {
    // 헤더 필드 스펙
    private static final List<FieldSpec> SPECS = List.of(
        new FieldSpec("messageType", 4, FieldSpec.FieldType.STRING),
        new FieldSpec("tradeDate", 8, FieldSpec.FieldType.DATE),
        new FieldSpec("tradeTime", 6, FieldSpec.FieldType.TIME),
        new FieldSpec("totalCount", 10, FieldSpec.FieldType.NUMBER)
    );
    
    public static List<FieldSpec> specs() {
        return SPECS;
    }
    
    public static int totalLength() {
        return SPECS.stream().mapToInt(FieldSpec::length).sum();
    }
}
