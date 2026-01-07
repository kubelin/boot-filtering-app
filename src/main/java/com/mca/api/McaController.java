package com.mca.api;

import com.mca.model.TargetSystemResponse;
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
     * 헬스 체크
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
