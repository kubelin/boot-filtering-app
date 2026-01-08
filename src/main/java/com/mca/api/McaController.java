package com.mca.api;

import com.mca.model.TargetSystemResponse;
import com.mca.parser.McaMessageParser;
import com.mca.parser.McaParserConfig;
import com.mca.service.McaProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MCA 로그 처리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mca")
@RequiredArgsConstructor
public class McaController {

    private final McaProcessingService service;
    private final McaMessageParser parser;

    /**
     * MCA 로그 처리 API
     */
    @PostMapping(value = "/process",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TargetSystemResponse> process(@RequestBody String mcaLog) {
        log.info("MCA 로그 처리 요청: {} bytes", mcaLog.length());

        TargetSystemResponse response = service.process(mcaLog);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * MCA 로그 처리 API (재시도 포함)
     */
    @PostMapping(value = "/process-retry",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TargetSystemResponse> processWithRetry(@RequestBody String mcaLog) {
        log.info("MCA 로그 처리 요청 (재시도 포함): {} bytes", mcaLog.length());

        TargetSystemResponse response = service.processWithRetry(mcaLog);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * MCA 로그 파싱 API (String 출력)
     */
    @PostMapping(value = "/parse",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> parse(@RequestBody String mcaLog) {
        log.info("MCA 로그 파싱 요청 (String): {} bytes", mcaLog.length());

        try {
            String result = parser.parseToString(mcaLog);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("MCA 로그 파싱 실패", e);
            return ResponseEntity.badRequest().body("파싱 실패: " + e.getMessage());
        }
    }

    /**
     * MCA 로그 파싱 API (JSON 출력)
     */
    @PostMapping(value = "/parse/json",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> parseJson(@RequestBody String mcaLog) {
        log.info("MCA 로그 파싱 요청 (JSON): {} bytes", mcaLog.length());

        try {
            String json = parser.parseToJson(mcaLog);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            log.error("MCA 로그 파싱 실패", e);
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * MCA 로그 파싱 API (커스텀 delimiter)
     */
    @PostMapping(value = "/parse/custom",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> parseCustom(
            @RequestBody String mcaLog,
            @RequestParam(defaultValue = "|") String delimiter) {
        log.info("MCA 로그 파싱 요청 (커스텀 delimiter='{}'): {} bytes",
                delimiter, mcaLog.length());

        try {
            // 커스텀 delimiter로 임시 설정 생성
            McaParserConfig customConfig = new McaParserConfig();
            customConfig.setDelimiter(delimiter);
            customConfig.setDataPrefix("c0");
            customConfig.setHeaderColumnCount(4);

            McaMessageParser customParser = new McaMessageParser(customConfig);
            String result = customParser.parseToString(mcaLog);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("MCA 로그 파싱 실패", e);
            return ResponseEntity.badRequest().body("파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
