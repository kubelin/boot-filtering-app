package com.mca.parser;

import com.mca.parser.model.HeaderFieldSpec;
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
     * 데이터 시작 마커 (기본: :|)
     */
    private String dataPrefix = ":|";

    /**
     * 데이터 종료 마커 (기본: [EXT])
     */
    private String dataSuffix = "[EXT]";

    /**
     * 헤더 컬럼 개수 (고정값, 제한 없음)
     * 예: 4, 10, 15, 20 등
     */
    private int headerColumnCount = 4;

    /**
     * 헤더 필드명 리스트 (옵션)
     * 미지정시 header0, header1, ... 자동 생성
     * @deprecated headerFieldSpecs 사용 권장
     */
    @Deprecated
    private List<String> headerFieldNames;

    /**
     * 헤더 필드 사양 리스트 (이름 + 고정 길이)
     * 파싱된 값이 고정 길이보다 작으면 공백으로 패딩
     */
    private List<HeaderFieldSpec> headerFieldSpecs;
}
