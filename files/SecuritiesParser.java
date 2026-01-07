package com.securities.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.parser.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 고정길이 증권 데이터 파서 (JDK 17 간소화 버전)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecuritiesParser {

    private final ObjectMapper objectMapper;

    /**
     * FLD 형식 파싱: 구분자 제거한 문자열 반환
     */
    public String parseToFld(String rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return "";
        }

        var dataStart = rawData.indexOf(SecuritiesDataRecord.MARKER);
        if (dataStart == -1) {
            log.error("Data marker not found");
            return "";
        }

        var dataSection = rawData.substring(dataStart + SecuritiesDataRecord.MARKER.length());
        return dataSection.replace("|", "");
    }

    /**
     * JSON 형식 파싱: 객체를 JSON으로 반환
     */
    public String parseToJson(String rawData) {
        try {
            var message = parseToObject(rawData);
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("JSON parsing failed", e);
            return "{}";
        }
    }

    /**
     * 객체로 파싱
     */
    private SecuritiesMessage parseToObject(String rawData) {
        var dataStart = rawData.indexOf(SecuritiesDataRecord.MARKER);
        if (dataStart == -1) {
            throw new IllegalArgumentException("Data marker not found");
        }

        // 헤더 파싱
        var headerData = rawData.substring(0, dataStart);
        var header = parseHeader(headerData);

        // 데이터부 파싱
        var dataSection = rawData.substring(dataStart + SecuritiesDataRecord.MARKER.length());
        var cleanData = dataSection.replace("|", "");
        var records = parseDataRecords(cleanData);

        return new SecuritiesMessage(header, records, System.currentTimeMillis());
    }

    /**
     * 헤더 파싱
     */
    private SecuritiesHeader parseHeader(String data) {
        var values = parseFixedLength(data, SecuritiesHeader.specs());
        return new SecuritiesHeader(
            values.get(0).toString(),
            values.get(1).toString(),
            values.get(2).toString(),
            (Long) values.get(3)
        );
    }

    /**
     * 데이터 레코드 파싱
     */
    private List<SecuritiesDataRecord> parseDataRecords(String data) {
        var records = new ArrayList<SecuritiesDataRecord>();
        var recordLength = SecuritiesDataRecord.recordLength();
        var position = 0;

        while (position + recordLength <= data.length()) {
            var recordData = data.substring(position, position + recordLength);
            var values = parseFixedLength(recordData, SecuritiesDataRecord.specs());
            
            records.add(new SecuritiesDataRecord(
                values.get(0).toString(),
                values.get(1).toString(),
                (Long) values.get(2),
                (Double) values.get(3),
                (Long) values.get(4)
            ));
            
            position += recordLength;
        }

        return records;
    }

    /**
     * 고정길이 데이터 파싱
     */
    private List<Object> parseFixedLength(String data, List<FieldSpec> specs) {
        var values = new ArrayList<Object>();
        var position = 0;

        for (var spec : specs) {
            if (position >= data.length()) break;

            var end = Math.min(position + spec.length(), data.length());
            var value = data.substring(position, end).trim();
            
            values.add(convertValue(value, spec.type()));
            position = end;
        }

        return values;
    }

    /**
     * 타입 변환
     */
    private Object convertValue(String value, FieldSpec.FieldType type) {
        if (value.isEmpty()) return null;

        try {
            return switch (type) {
                case NUMBER -> Long.parseLong(value);
                case DECIMAL -> Double.parseDouble(value);
                case STRING, DATE, TIME -> value;
            };
        } catch (NumberFormatException e) {
            log.warn("Type conversion failed: value='{}', type={}", value, type);
            return value;
        }
    }
}
