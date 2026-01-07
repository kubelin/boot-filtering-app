package com.securities.parser;

import com.securities.parser.model.SecuritiesDataRecord;
import com.securities.parser.model.SecuritiesHeader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class SecuritiesParserTest {

    @Autowired
    private SecuritiesParser parser;

    private static final String SAMPLE = 
        "STCK20240101120000000000002" +
        ":| c0" +
        "005930삼성전자              0000075000+00350000000012345678|" +
        "000660SK하이닉스          0000142000+01200000000098765432";

    @Test
    void testSpecs() {
        assertThat(SecuritiesHeader.totalLength()).isEqualTo(28);
        assertThat(SecuritiesDataRecord.recordLength()).isEqualTo(57);
    }

    @Test
    void testParseToFld() {
        var result = parser.parseToFld(SAMPLE);
        
        assertThat(result).doesNotContain("|");
        assertThat(result).contains("005930", "삼성전자");
    }

    @Test
    void testParseToJson() {
        var result = parser.parseToJson(SAMPLE);
        
        assertThat(result).contains("header", "dataRecords");
        assertThat(result).contains("STCK", "005930");
    }

    @Test
    void testPerformance() {
        var iterations = 10000;
        
        // Warm up
        for (int i = 0; i < 1000; i++) {
            parser.parseToFld(SAMPLE);
        }
        
        var start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            parser.parseToFld(SAMPLE);
        }
        var elapsed = System.nanoTime() - start;
        
        var avgMicros = (double) elapsed / iterations / 1000;
        System.out.printf("평균 파싱 시간: %.2f μs%n", avgMicros);
        
        assertThat(avgMicros).isLessThan(100.0);
    }

    @Test
    void testEmptyData() {
        assertThat(parser.parseToFld("")).isEmpty();
        assertThat(parser.parseToJson("")).isEqualTo("{}");
    }
}
