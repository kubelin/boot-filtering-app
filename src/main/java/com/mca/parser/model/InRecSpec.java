package com.mca.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * InRec 사양 (meta 테이블)
 * - InRec 이름과 내부 필드 목록 정의
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InRecSpec {

    /**
     * InRec 이름 (예: "InRec1", "InRec2")
     */
    private String name;

    /**
     * InRec 내부 필드 목록 (meta-detail)
     */
    private List<RecordFieldSpec> fields;

    /**
     * InRec 전체 길이 계산
     */
    public int getTotalLength() {
        return fields.stream()
                .mapToInt(RecordFieldSpec::getLength)
                .sum();
    }
}
