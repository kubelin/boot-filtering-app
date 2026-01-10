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
    @DisplayName("MCA 로그로부터 FLD 메시지 생성 (delimiter 제거, 공백 보존)")
    void testBuildMessage_fromRawLog() {
        // Given
        String rawLog = "metadata :|100|A01|TX123|200|data1|data2|data3[EXT]";

        // When
        FldMessage message = builder.buildMessage(rawLog);

        // Then - delimiter 제거, 고정 길이 패딩 적용
        assertThat(message).isNotNull();
        // length(10) + messageType(3) + transactionId(36) + code(4) = 53자
        assertThat(message.header()).hasSize(53);
        assertThat(message.header()).startsWith("100       ");  // length: 10자리
        assertThat(message.header()).contains("A01");           // messageType: 3자리
        assertThat(message.data()).isEqualTo("data1data2data3");
        assertThat(message.totalLength()).isGreaterThan(0);
    }

    @Test
    @DisplayName("McaMessage로부터 FLD 메시지 생성")
    void testBuildMessage_fromMcaMessage() {
        // Given
        String rawLog = ":|h1|h2|h3|h4|body1|body2[EXT]";
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
        String rawLog = ":|header1|header2|header3|header4|bodyData[EXT]";

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
        String rawLog = ":|100   |A01  |TX123              |200     |data with spaces[EXT]";

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
        String rawLog = ":|h1|h2|h3|h4|body[EXT]";

        // When
        FldMessage message = builder.buildMessage(rawLog);
        String delimitedString = message.toDelimitedString();

        // Then - header와 data 사이에만 delimiter (header는 고정 길이 패딩 적용)
        assertThat(delimitedString).contains("|");
        // h1(10자) + h2(3자) + h3(36자) + h4(4자) = 53자
        String[] parts = delimitedString.split("\\|");
        assertThat(parts).hasSize(2);
        assertThat(parts[0]).hasSize(53);  // 패딩된 헤더
        assertThat(parts[1]).isEqualTo("body");
    }
}
