package com.mca.parser;

import com.mca.parser.model.McaLogData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCA 로그 파서
 */
@Slf4j
@Component
public class McaLogParser {

    // MCA 로그 구분자
    private static final String FIELD_DELIMITER = "\\|";

    /**
     * MCA 로그 문자열 파싱
     */
    public McaLogData parse(String rawLog) {
        if (rawLog == null || rawLog.isEmpty()) {
            throw new IllegalArgumentException("MCA 로그가 비어있습니다");
        }

        log.debug("MCA 로그 파싱 시작: {} bytes", rawLog.length());

        try {
            Map<String, String> fields = parseFields(rawLog);
            return new McaLogData(rawLog, fields, LocalDateTime.now());
        } catch (Exception e) {
            log.error("MCA 로그 파싱 실패", e);
            throw new RuntimeException("MCA 로그 파싱 중 오류 발생", e);
        }
    }

    /**
     * 파일에서 MCA 로그 파싱
     */
    public List<McaLogData> parseFile(Path logFile) {
        try {
            log.info("MCA 로그 파일 읽기: {}", logFile);

            return Files.readAllLines(logFile).stream()
                .filter(line -> !line.trim().isEmpty())
                .map(this::parse)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("MCA 로그 파일 읽기 실패: {}", logFile, e);
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }

    /**
     * 필드 파싱
     */
    private Map<String, String> parseFields(String rawLog) {
        Map<String, String> fields = new HashMap<>();

        // | 구분자로 분리
        String[] parts = rawLog.split(FIELD_DELIMITER);

        // 기본 필드 추출
        if (parts.length >= 1) {
            fields.put("messageType", parts[0].trim());
        }
        if (parts.length >= 2) {
            fields.put("date", parts[1].trim());
        }
        if (parts.length >= 3) {
            fields.put("time", parts[2].trim());
        }

        // 나머지 데이터를 body로 저장
        if (parts.length > 3) {
            StringBuilder body = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                if (i > 3) {
                    body.append("|");
                }
                body.append(parts[i]);
            }
            fields.put("body", body.toString());
        }

        log.debug("파싱된 필드 개수: {}", fields.size());
        return fields;
    }
}
