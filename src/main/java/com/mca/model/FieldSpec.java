package com.mca.model;

/**
 * 고정길이 필드 정의 (Java 17 record)
 */
public record FieldSpec(
    String name,
    int length,
    FieldType type
) {
    public enum FieldType {
        STRING, NUMBER, DECIMAL, DATE, TIME
    }
}
