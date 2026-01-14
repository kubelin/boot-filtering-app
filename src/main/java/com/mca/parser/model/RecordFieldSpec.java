package com.mca.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InRec 내부 필드 사양 (meta-detail 테이블)
 * - 필드명과 고정 길이 정의
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordFieldSpec {

    /**
     * 필드 이름
     */
    private String name;

    /**
     * 필드 고정 길이
     */
    private int length;
}
