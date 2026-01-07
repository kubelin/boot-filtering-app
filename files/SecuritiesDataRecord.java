package com.securities.parser.model;

import java.util.List;

/**
 * 데이터부 레코드 정의 (Java 17 record)
 * 
 * 예시: 종목코드(6) + 종목명(20) + 현재가(10) + 등락률(6) + 거래량(15)
 */
public record SecuritiesDataRecord(
    String stockCode,
    String stockName,
    Long currentPrice,
    Double changeRate,
    Long volume
) {
    // 데이터 시작 마커
    public static final String MARKER = ":| c0";
    
    // 데이터부 필드 스펙
    private static final List<FieldSpec> SPECS = List.of(
        new FieldSpec("stockCode", 6, FieldSpec.FieldType.STRING),
        new FieldSpec("stockName", 20, FieldSpec.FieldType.STRING),
        new FieldSpec("currentPrice", 10, FieldSpec.FieldType.NUMBER),
        new FieldSpec("changeRate", 6, FieldSpec.FieldType.DECIMAL),
        new FieldSpec("volume", 15, FieldSpec.FieldType.NUMBER)
    );
    
    public static List<FieldSpec> specs() {
        return SPECS;
    }
    
    public static int recordLength() {
        return SPECS.stream().mapToInt(FieldSpec::length).sum();
    }
}
