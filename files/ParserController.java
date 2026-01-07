package com.securities.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 증권 데이터 파싱 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/parse")
@RequiredArgsConstructor
public class ParserController {

    private final SecuritiesParser parser;

    @PostMapping(value = "/fld", 
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public String fld(@RequestBody String rawData) {
        log.info("FLD parsing request, size: {} bytes", rawData.length());
        return parser.parseToFld(rawData);
    }

    @PostMapping(value = "/json",
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public String json(@RequestBody String rawData) {
        log.info("JSON parsing request, size: {} bytes", rawData.length());
        return parser.parseToJson(rawData);
    }
}
