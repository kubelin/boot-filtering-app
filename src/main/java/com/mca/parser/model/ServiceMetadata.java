package com.mca.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 서비스별 메타데이터
 * - interface 이름과 InRec 목록 정의
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetadata {

    /**
     * 서비스 인터페이스 이름 (예: "eab0001p0")
     */
    private String interfaceName;

    /**
     * 헤더 필드 사양 목록
     */
    private List<HeaderFieldSpec> headerFields;

    /**
     * InRec 목록 (meta 테이블)
     */
    private List<InRecSpec> inRecs;
}
