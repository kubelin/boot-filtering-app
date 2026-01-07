package com.mca.parser;

import com.mca.parser.model.McaLogData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class McaLogParserTest {

    @Autowired
    private McaLogParser parser;

    private static final String SAMPLE_LOG = "MCA0|20240101|120000|SAMPLE_DATA";

    @Test
    void testParse() {
        // When
        McaLogData result = parser.parse(SAMPLE_LOG);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.rawData()).isEqualTo(SAMPLE_LOG);
        assertThat(result.getField("messageType")).isEqualTo("MCA0");
        assertThat(result.getField("date")).isEqualTo("20240101");
        assertThat(result.getField("time")).isEqualTo("120000");
        assertThat(result.getField("body")).isEqualTo("SAMPLE_DATA");
    }

    @Test
    void testParseEmptyLog() {
        // When & Then
        assertThatThrownBy(() -> parser.parse(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비어있습니다");
    }

    @Test
    void testParseNullLog() {
        // When & Then
        assertThatThrownBy(() -> parser.parse(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
