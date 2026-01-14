package com.mca.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.parser.metadata.MetadataRepository;
import com.mca.parser.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FLD 형식 데이터 → JSON 변환기
 * - 메타데이터 기반 동적 파싱
 * - header + data (InRec1, InRec2...) 구조 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FldToJsonConverter {

    private final MetadataRepository metadataRepository;
    private final ObjectMapper objectMapper;

    /**
     * FLD 데이터를 JSON으로 변환
     *
     * @param fldData FLD 형식 문자열 (고정 길이)
     * @param interfaceName 서비스 인터페이스 이름
     * @return JSON 문자열
     */
    public String convertToJson(String fldData, String interfaceName) {
        log.debug("FLD → JSON 변환 시작: interface={}, length={}", interfaceName, fldData.length());

        // 1. 메타데이터 조회
        ServiceMetadata metadata = metadataRepository.findByInterfaceName(interfaceName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "메타데이터를 찾을 수 없습니다: " + interfaceName
                ));

        // 2. FLD 데이터 파싱
        Map<String, Object> result = new LinkedHashMap<>();
        int offset = 0;

        // 3. Header 파싱
        Map<String, String> headerMap = parseHeader(fldData, metadata.getHeaderFields(), offset);
        result.put("header", headerMap);
        offset += calculateTotalLength(metadata.getHeaderFields());

        // 4. Data 파싱 (InRec들)
        Map<String, Map<String, String>> dataMap = parseData(fldData, metadata.getInRecs(), offset);
        result.put("data", dataMap);

        // 5. JSON 변환
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
            log.debug("FLD → JSON 변환 완료: {} bytes", json.length());
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    /**
     * Header 파싱
     */
    private Map<String, String> parseHeader(String fldData,
                                              java.util.List<HeaderFieldSpec> headerFields,
                                              int startOffset) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        int offset = startOffset;

        for (HeaderFieldSpec field : headerFields) {
            String value = extractField(fldData, offset, field.getLength());
            headerMap.put(field.getName(), value);
            offset += field.getLength();
        }

        return headerMap;
    }

    /**
     * Data 파싱 (InRec들)
     */
    private Map<String, Map<String, String>> parseData(String fldData,
                                                         java.util.List<InRecSpec> inRecs,
                                                         int startOffset) {
        Map<String, Map<String, String>> dataMap = new LinkedHashMap<>();
        int offset = startOffset;

        for (InRecSpec inRec : inRecs) {
            Map<String, String> inRecMap = parseInRec(fldData, inRec, offset);
            dataMap.put(inRec.getName(), inRecMap);
            offset += inRec.getTotalLength();
        }

        return dataMap;
    }

    /**
     * 단일 InRec 파싱
     */
    private Map<String, String> parseInRec(String fldData,
                                             InRecSpec inRec,
                                             int startOffset) {
        Map<String, String> inRecMap = new LinkedHashMap<>();
        int offset = startOffset;

        for (RecordFieldSpec field : inRec.getFields()) {
            String value = extractField(fldData, offset, field.getLength());
            inRecMap.put(field.getName(), value);
            offset += field.getLength();
        }

        return inRecMap;
    }

    /**
     * FLD 데이터에서 필드 추출
     */
    private String extractField(String fldData, int offset, int length) {
        if (offset + length > fldData.length()) {
            log.warn("FLD 데이터 길이 부족: offset={}, length={}, total={}",
                    offset, length, fldData.length());
            return "";
        }

        return fldData.substring(offset, offset + length).trim();
    }

    /**
     * 헤더 전체 길이 계산
     */
    private int calculateTotalLength(java.util.List<HeaderFieldSpec> fields) {
        return fields.stream()
                .mapToInt(HeaderFieldSpec::getLength)
                .sum();
    }
}
