package com.mca.fld;

import com.mca.fld.model.FldMessage;
import com.mca.parser.McaMessageParser;
import com.mca.parser.model.McaMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("FLD 메시지 빌더 테스트 (간소화)")
class FldMessageBuilderTest {

    @Autowired
    private FldMessageBuilder builder;

    @Autowired
    private McaMessageParser parser;

    @Test
    @DisplayName("MCA 로그로부터 FLD 메시지 생성 (공백 포함 그대로)")
    void testBuildMessage_fromRawLog() {
        // Given
        String rawLog = "metadata c0|100|A01|TX123|200|data1|data2|data3";

        // When
        FldMessage message = builder.buildMessage(rawLog);

        // Then
        assertThat(message).isNotNull();
        assertThat(message.header()).isEqualTo("100|A01|TX123|200");
        assertThat(message.data()).isEqualTo("data1|data2|data3");
        assertThat(message.totalLength()).isGreaterThan(0);
    }

    @Test
    @DisplayName("McaMessage로부터 FLD 메시지 생성")
    void testBuildMessage_fromMcaMessage() {
        // Given
        String rawLog = "c0|h1|h2|h3|h4|body1|body2";
        McaMessage mcaMessage = parser.parse(rawLog);

        // When
        FldMessage fldMessage = builder.buildMessage(mcaMessage);

        // Then
        assertThat(fldMessage).isNotNull();
        assertThat(fldMessage.header()).isEqualTo(mcaMessage.header());
        assertThat(fldMessage.data()).isEqualTo(mcaMessage.body());
    }

    @Test
    @DisplayName("FLD 문자열 변환 (구분자 없음)")
    void testFldStringFormat() {
        // Given
        String rawLog = "c0|header1|header2|header3|header4|bodyData";

        // When
        FldMessage message = builder.buildMessage(rawLog);
        String fldString = message.toFldString();

        // Then
        // FLD는 구분자 없이 헤더+데이터 연결
        assertThat(fldString).contains("header1");
        assertThat(fldString).contains("bodyData");
        assertThat(message.totalLength()).isEqualTo(fldString.length());
    }

    @Test
    @DisplayName("공백 포함 데이터 처리")
    void testWithWhitespace() {
        // Given - 공백 포함 데이터
        String rawLog = "c0|100   |A01  |TX123              |200     |data with spaces";

        // When
        FldMessage message = builder.buildMessage(rawLog);

        // Then - 공백이 그대로 유지됨
        assertThat(message.header()).contains("100   ");
        assertThat(message.header()).contains("A01  ");
        assertThat(message.data()).contains("data with spaces");
    }

    @Test
    @DisplayName("구분자 포함 문자열 (디버깅용)")
    void testDelimitedString() {
        // Given
        String rawLog = "c0|h1|h2|h3|h4|body";

        // When
        FldMessage message = builder.buildMessage(rawLog);
        String delimitedString = message.toDelimitedString();

        // Then
        assertThat(delimitedString).contains("|");
        assertThat(delimitedString).isEqualTo("h1|h2|h3|h4|body");
    }
}
