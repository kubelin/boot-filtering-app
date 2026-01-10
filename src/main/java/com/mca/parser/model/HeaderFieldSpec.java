package com.mca.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 헤더 필드 사양 (이름 + 고정 길이)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderFieldSpec {

    /**
     * 필드 이름
     */
    private String name;

    /**
     * 필드 고정 길이
     */
    private int length;
}
