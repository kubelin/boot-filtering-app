package com.mca.fld;

import com.mca.fld.model.FldMessage;
import com.mca.parser.McaLogParser;
import com.mca.parser.model.McaLogData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class FldMessageBuilderTest {

    @Autowired
    private FldMessageBuilder builder;

    @Autowired
    private McaLogParser parser;

    private static final String SAMPLE_LOG = "MCA0|20240101|120000|TEST_DATA";

    @Test
    void testBuildMessage() {
        // Given
        McaLogData logData = parser.parse(SAMPLE_LOG);

        // When
        FldMessage message = builder.buildMessage(logData);

        // Then
        assertThat(message).isNotNull();
        assertThat(message.header()).isNotNull();
        assertThat(message.body()).isNotNull();
        assertThat(message.toFldString()).isNotEmpty();
    }

    @Test
    void testFldMessageFormat() {
        // Given
        McaLogData logData = parser.parse(SAMPLE_LOG);

        // When
        FldMessage message = builder.buildMessage(logData);
        String fldString = message.toFldString();

        // Then
        assertThat(fldString).doesNotContain("|"); // 구분자 제거 확인
        assertThat(message.totalLength()).isGreaterThan(0);
    }
}
