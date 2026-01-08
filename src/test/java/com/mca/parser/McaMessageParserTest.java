package com.mca.parser;

import com.mca.parser.model.McaMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MCA 메시지 파서 테스트")
class McaMessageParserTest {

    private McaMessageParser parser;
    private McaParserConfig config;

    @BeforeEach
    void setUp() {
        config = new McaParserConfig();
        config.setDelimiter("|");
        config.setDataPrefix("c0");
        config.setHeaderColumnCount(4);
        config.setHeaderFieldNames(List.of("length", "messageType", "transactionId", "code"));

        parser = new McaMessageParser(config);
    }

    @Test
    @DisplayName("기본 파싱 - String 출력 (delimiter 제거)")
    void parseToString_기본() {
        // Given
        String rawLog = "metadata c0|100|A01|TX123|200|bodyData1|bodyData2";

        // When
        String result = parser.parseToString(rawLog);

        // Then - delimiter 제거, 공백 보존
        assertThat(result).isEqualTo("100A01TX123200bodyData1bodyData2");
    }

    @Test
    @DisplayName("기본 파싱 - 객체 출력 (delimiter 제거)")
    void parse_기본() {
        // Given
        String rawLog = "metadata c0|100|A01|TX123|200|bodyData1|bodyData2";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - delimiter 제거, 공백 보존
        assertThat(result.header()).isEqualTo("100A01TX123200");
        assertThat(result.body()).isEqualTo("bodyData1bodyData2");
        assertThat(result.headerFields()).hasSize(4);
        assertThat(result.headerFields().get("length")).isEqualTo("100");
        assertThat(result.headerFields().get("messageType")).isEqualTo("A01");
        assertThat(result.bodyFields()).hasSize(2);
    }

    @Test
    @DisplayName("헤더만 있는 경우 (delimiter 제거)")
    void parse_헤더만() {
        // Given
        String rawLog = "c0|100|A01|TX123|200";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - delimiter 제거
        assertThat(result.header()).isEqualTo("100A01TX123200");
        assertThat(result.body()).isEmpty();
        assertThat(result.bodyFields()).isEmpty();
    }

    @Test
    @DisplayName("헤더 10개 파싱")
    void parse_헤더10개() {
        // Given
        config.setHeaderColumnCount(10);
        parser = new McaMessageParser(config);

        String rawLog = "c0|1|2|3|4|5|6|7|8|9|10|b1|b2|b3";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.headerFields()).hasSize(10);
        assertThat(result.bodyFields()).hasSize(3);
    }

    @Test
    @DisplayName("헤더 15개 파싱")
    void parse_헤더15개() {
        // Given
        config.setHeaderColumnCount(15);
        config.setHeaderFieldNames(null);  // 자동 생성 모드
        parser = new McaMessageParser(config);

        String rawLog = "c0|h1|h2|h3|h4|h5|h6|h7|h8|h9|h10|h11|h12|h13|h14|h15|body1|body2";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.headerFields()).hasSize(15);
        assertThat(result.bodyFields()).hasSize(2);
        assertThat(result.headerFields().get("header0")).isEqualTo("h1");
        assertThat(result.headerFields().get("header14")).isEqualTo("h15");
    }

    @Test
    @DisplayName("커스텀 delimiter 파싱 (쉼표) - delimiter 제거")
    void parse_커스텀delimiter() {
        // Given
        config.setDelimiter(",");
        config.setHeaderColumnCount(3);
        parser = new McaMessageParser(config);

        String rawLog = "c0,h1,h2,h3,body1,body2";

        // When
        String result = parser.parseToString(rawLog);

        // Then - delimiter 제거
        assertThat(result).isEqualTo("h1h2h3body1body2");
    }

    @Test
    @DisplayName("실제 샘플 데이터 파싱 (delimiter 제거)")
    void parse_실제샘플() {
        // Given
        String rawLog = "(2025-12-26 13:15:17.675480] HYBRID<GROUP_BNEXIA> < 30653> MCA>HOST ERRI] " +
                "[pfaa003p ==pfaa003p:A01):c0|0000000578|A01|000000007c3f75d90000000011f0e211|84|" +
                "nzeustest|nzeustest||0|||045.082.011.148";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - delimiter 제거
        assertThat(result.header()).isEqualTo("0000000578A01000000007c3f75d90000000011f0e21184");
        assertThat(result.headerFields().get("length")).isEqualTo("0000000578");
        assertThat(result.headerFields().get("messageType")).isEqualTo("A01");
        assertThat(result.bodyFields()).contains("nzeustest", "nzeustest", "", "0");
    }

    @Test
    @DisplayName("JSON 출력")
    void parseToJson() {
        // Given
        String rawLog = "c0|100|A01|TX123|200|data1|data2";

        // When
        String json = parser.parseToJson(rawLog);

        // Then
        assertThat(json).contains("\"header\"");
        assertThat(json).contains("\"body\"");
        assertThat(json).contains("\"length\" : \"100\"");
        assertThat(json).contains("\"messageType\" : \"A01\"");
    }

    @Test
    @DisplayName("빈 바디 필드 처리")
    void parse_빈필드() {
        // Given
        String rawLog = "c0|100|A01|TX123|200||empty||field";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.bodyFields()).hasSize(4);
        assertThat(result.bodyFields().get(0)).isEmpty();
        assertThat(result.bodyFields().get(1)).isEqualTo("empty");
        assertThat(result.bodyFields().get(2)).isEmpty();
        assertThat(result.bodyFields().get(3)).isEqualTo("field");
    }

    @Test
    @DisplayName("데이터 마커 없는 경우 예외 발생")
    void parse_마커없음_예외() {
        // Given
        String rawLog = "no marker here|100|A01";

        // When & Then
        assertThatThrownBy(() -> parser.parse(rawLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("데이터 시작 마커를 찾을 수 없습니다");
    }
}
