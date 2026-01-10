package com.mca.parser;

import com.mca.parser.model.McaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * MCA 메시지 파서
 * - :| 이후 [EXT] 이전 데이터를 delimiter로 분리
 * - 설정된 개수만큼 헤더/바디 분리
 * - String 또는 JSON 형식으로 출력
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McaMessageParser {

    private final McaParserConfig config;

    /**
     * MCA 로그 파싱 → String 출력 (전문 통신용)
     * delimiter는 제거하고 공백은 그대로 유지
     *
     * @param rawLog MCA 로그 원본
     * @return "헤더1헤더2...바디1바디2..." (delimiter 제거, 공백 보존)
     */
    public String parseToString(String rawLog) {
        log.debug("MCA 로그 파싱 시작 (String): {} bytes", rawLog.length());

        // 1. :| 이후 [EXT] 이전 데이터 추출
        String data = extractData(rawLog);

        // 2. delimiter로 분리
        String[] fields = splitFields(data);

        // 3. delimiter 없이 연결 (공백은 그대로 유지)
        String result = String.join("", fields);

        log.debug("파싱 완료 (String): {} 필드, {} bytes", fields.length, result.length());
        return result;
    }

    /**
     * MCA 로그 파싱 → JSON 출력
     *
     * @param rawLog MCA 로그 원본
     * @return {"header": {...}, "body": [...]}
     */
    public String parseToJson(String rawLog) {
        log.debug("MCA 로그 파싱 시작 (JSON): {} bytes", rawLog.length());

        McaMessage message = parse(rawLog);
        String json = message.toJson();

        log.debug("파싱 완료 (JSON): {} bytes", json.length());
        return json;
    }

    /**
     * MCA 로그 파싱 → 구조화된 객체
     *
     * @param rawLog MCA 로그 원본
     * @return McaMessage 객체
     */
    public McaMessage parse(String rawLog) {
        log.debug("MCA 로그 파싱 시작 (Object): {} bytes", rawLog.length());

        // 1. :| 이후 [EXT] 이전 데이터 추출
        String data = extractData(rawLog);

        // 2. delimiter로 분리
        String[] fields = splitFields(data);

        // 3. 헤더/바디 분리
        int headerCount = config.getHeaderColumnCount();
        String[] headerFields = extractHeaderFields(fields, headerCount);
        String[] bodyFields = extractBodyFields(fields, headerCount);

        // 4. 헤더/바디 문자열 생성 (delimiter 제거, 공백 보존)
        String headerStr = String.join("", headerFields);
        String bodyStr = String.join("", bodyFields);

        // 5. 필드 맵 생성 (JSON용)
        Map<String, String> headerMap = buildHeaderMap(headerFields);
        List<String> bodyList = Arrays.asList(bodyFields);

        log.debug("파싱 완료: 헤더 {} 필드, 바디 {} 필드", headerFields.length, bodyFields.length);

        return new McaMessage(
            headerStr,
            bodyStr,
            headerMap,
            bodyList,
            Instant.now()
        );
    }

    /**
     * :| 이후 [EXT] 이전 데이터 추출
     */
    private String extractData(String rawLog) {
        String prefix = config.getDataPrefix();
        String suffix = config.getDataSuffix();

        int dataStart = rawLog.indexOf(prefix);
        if (dataStart == -1) {
            throw new IllegalArgumentException(
                "데이터 시작 마커를 찾을 수 없습니다: " + prefix
            );
        }

        // :| 이후부터 추출
        String afterPrefix = rawLog.substring(dataStart + prefix.length());

        // [EXT] 위치 찾기
        int dataEnd = afterPrefix.indexOf(suffix);
        if (dataEnd != -1) {
            afterPrefix = afterPrefix.substring(0, dataEnd);
        }

        // 시작이 delimiter면 제거
        String delimiter = config.getDelimiter();
        if (afterPrefix.startsWith(delimiter)) {
            afterPrefix = afterPrefix.substring(delimiter.length());
        }

        return afterPrefix.trim();
    }

    /**
     * delimiter로 필드 분리
     */
    private String[] splitFields(String data) {
        String delimiter = config.getDelimiter();
        String escapedDelimiter = Pattern.quote(delimiter);
        return data.split(escapedDelimiter, -1);  // -1: 빈 문자열도 포함
    }

    /**
     * 헤더 필드 추출
     */
    private String[] extractHeaderFields(String[] allFields, int headerCount) {
        int count = Math.min(headerCount, allFields.length);
        return Arrays.copyOfRange(allFields, 0, count);
    }

    /**
     * 바디 필드 추출
     */
    private String[] extractBodyFields(String[] allFields, int headerCount) {
        if (allFields.length <= headerCount) {
            return new String[0];
        }
        return Arrays.copyOfRange(allFields, headerCount, allFields.length);
    }

    /**
     * 헤더 필드 맵 생성 (JSON용)
     * - headerFieldSpecs가 있으면 고정 길이 패딩 적용
     * - 없으면 기존 방식 (headerFieldNames 또는 자동 생성)
     */
    private Map<String, String> buildHeaderMap(String[] headerFields) {
        Map<String, String> map = new LinkedHashMap<>();

        // 1. headerFieldSpecs 우선 사용 (고정 길이 패딩)
        if (config.getHeaderFieldSpecs() != null && !config.getHeaderFieldSpecs().isEmpty()) {
            var fieldSpecs = config.getHeaderFieldSpecs();

            for (int i = 0; i < headerFields.length; i++) {
                String fieldName;
                int fieldLength = 0;

                // Spec에서 필드명과 길이 가져오기
                if (i < fieldSpecs.size()) {
                    fieldName = fieldSpecs.get(i).getName();
                    fieldLength = fieldSpecs.get(i).getLength();
                } else {
                    fieldName = "header" + i;
                }

                // 값에 공백 패딩 적용
                String paddedValue = padValue(headerFields[i], fieldLength);
                map.put(fieldName, paddedValue);
            }
        }
        // 2. 기존 방식 (headerFieldNames 또는 자동 생성)
        else {
            var fieldNames = config.getHeaderFieldNames();

            for (int i = 0; i < headerFields.length; i++) {
                String fieldName;

                if (fieldNames != null && i < fieldNames.size()) {
                    fieldName = fieldNames.get(i);
                } else {
                    fieldName = "header" + i;
                }

                map.put(fieldName, headerFields[i]);
            }
        }

        return map;
    }

    /**
     * 값에 공백 패딩 적용
     * - 값의 길이가 지정된 길이보다 작으면 오른쪽에 공백 추가
     * - 값의 길이가 지정된 길이보다 크거나 같으면 그대로 반환
     *
     * @param value 원본 값
     * @param length 목표 길이 (0이면 패딩 안 함)
     * @return 패딩된 값
     */
    private String padValue(String value, int length) {
        if (length <= 0 || value.length() >= length) {
            return value;
        }

        // 오른쪽에 공백 추가
        return String.format("%-" + length + "s", value);
    }
}
