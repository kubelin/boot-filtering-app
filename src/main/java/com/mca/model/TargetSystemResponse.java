package com.mca.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 타겟 시스템 응답 모델
 */
public record TargetSystemResponse(
    boolean success,
    int code,
    String message,
    LocalDate date,
    Instant timestamp
) {
    /**
     * 성공 응답 생성
     */
    public static TargetSystemResponse success(int code, String message) {
        return new TargetSystemResponse(
            true,
            code,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }

    /**
     * 실패 응답 생성
     */
    public static TargetSystemResponse failure(String message) {
        return new TargetSystemResponse(
            false,
            500,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }

    /**
     * 커스텀 응답 생성
     */
    public static TargetSystemResponse of(boolean success, int code, String message) {
        return new TargetSystemResponse(
            success,
            code,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }
}
