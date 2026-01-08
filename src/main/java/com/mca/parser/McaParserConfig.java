package com.mca.parser;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCA 파서 설정
 */
@Data
@Component
@ConfigurationProperties(prefix = "mca.parser")
public class McaParserConfig {

    /**
     * 필드 구분자 (기본: |)
     */
    private String delimiter = "|";

    /**
     * 데이터 시작 마커 (기본: c0)
     */
    private String dataPrefix = "c0";

    /**
     * 헤더 컬럼 개수 (고정값, 제한 없음)
     * 예: 4, 10, 15, 20 등
     */
    private int headerColumnCount = 4;

    /**
     * 헤더 필드명 리스트 (옵션)
     * 미지정시 header0, header1, ... 자동 생성
     */
    private List<String> headerFieldNames;
}
