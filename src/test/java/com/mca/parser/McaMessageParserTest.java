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
        config.setDataPrefix(":|");
        config.setDataSuffix("[EXT]");
        config.setHeaderColumnCount(4);
        config.setHeaderFieldNames(List.of("length", "messageType", "transactionId", "code"));

        parser = new McaMessageParser(config);
    }

    @Test
    @DisplayName("기본 파싱 - String 출력 (delimiter 제거)")
    void parseToString_기본() {
        // Given
        String rawLog = "metadata :|100|A01|TX123|200|bodyData1|bodyData2[EXT]";

        // When
        String result = parser.parseToString(rawLog);

        // Then - delimiter 제거, 공백 보존
        assertThat(result).isEqualTo("100A01TX123200bodyData1bodyData2");
    }

    @Test
    @DisplayName("기본 파싱 - 객체 출력 (delimiter 제거)")
    void parse_기본() {
        // Given
        String rawLog = "metadata :|100|A01|TX123|200|bodyData1|bodyData2[EXT]";

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
        String rawLog = ":|100|A01|TX123|200[EXT]";

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

        String rawLog = ":|1|2|3|4|5|6|7|8|9|10|b1|b2|b3[EXT]";

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

        String rawLog = ":|h1|h2|h3|h4|h5|h6|h7|h8|h9|h10|h11|h12|h13|h14|h15|body1|body2[EXT]";

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

        String rawLog = ":|,h1,h2,h3,body1,body2[EXT]";

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
                "[pfaa003p ==pfaa003p:A01):|0000000578|A01|000000007c3f75d90000000011f0e211|84|" +
                "nzeustest|nzeustest||0|||045.082.011.148[EXT]";

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
        String rawLog = ":|100|A01|TX123|200|data1|data2[EXT]";

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
        String rawLog = ":|100|A01|TX123|200||empty||field[EXT]";

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

    @Test
    @DisplayName("헤더 필드 고정 길이 패딩 (짧은 값)")
    void parse_고정길이패딩_짧은값() {
        // Given
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("length", 10),
            new com.mca.parser.model.HeaderFieldSpec("messageType", 5),
            new com.mca.parser.model.HeaderFieldSpec("code", 3)
        ));
        config.setHeaderColumnCount(3);
        parser = new McaMessageParser(config);

        String rawLog = ":|100|A01|84[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - 값이 고정 길이보다 작으면 공백 패딩
        assertThat(result.headerFields()).hasSize(3);
        assertThat(result.headerFields().get("length")).isEqualTo("100       ");  // 10자리
        assertThat(result.headerFields().get("messageType")).isEqualTo("A01  ");  // 5자리
        assertThat(result.headerFields().get("code")).isEqualTo("84 ");           // 3자리
    }

    @Test
    @DisplayName("헤더 필드 고정 길이 패딩 (긴 값 - 오른쪽 자르기)")
    void parse_고정길이패딩_긴값() {
        // Given
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("field1", 5),
            new com.mca.parser.model.HeaderFieldSpec("field2", 3)
        ));
        config.setHeaderColumnCount(2);
        parser = new McaMessageParser(config);

        String rawLog = ":|veryLongValue|12345[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - 값이 고정 길이보다 크면 오른쪽 자르기
        assertThat(result.headerFields()).hasSize(2);
        assertThat(result.headerFields().get("field1")).isEqualTo("veryL");  // 5자로 자름
        assertThat(result.headerFields().get("field2")).isEqualTo("123");    // 3자로 자름
    }

    @Test
    @DisplayName("헤더 필드 고정 길이 0 (패딩 안 함)")
    void parse_고정길이0_패딩안함() {
        // Given
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("field1", 0),
            new com.mca.parser.model.HeaderFieldSpec("field2", 0)
        ));
        config.setHeaderColumnCount(2);
        parser = new McaMessageParser(config);

        String rawLog = ":|abc|12[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - 길이 0이면 패딩 안 함
        assertThat(result.headerFields().get("field1")).isEqualTo("abc");
        assertThat(result.headerFields().get("field2")).isEqualTo("12");
    }

    @Test
    @DisplayName("헤더 필드 고정 길이 - 정확히 같은 길이")
    void parse_고정길이_정확한길이() {
        // Given
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("field1", 3),
            new com.mca.parser.model.HeaderFieldSpec("field2", 5)
        ));
        config.setHeaderColumnCount(2);
        parser = new McaMessageParser(config);

        String rawLog = ":|ABC|12345[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then - 길이가 정확히 같으면 그대로 반환
        assertThat(result.headerFields().get("field1")).isEqualTo("ABC");
        assertThat(result.headerFields().get("field2")).isEqualTo("12345");
    }

    @Test
    @DisplayName("헤더 필드 고정 길이 - 통합 테스트 (짧음/같음/긺)")
    void parse_고정길이_통합테스트() {
        // Given
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("short", 10),   // 짧은 값
            new com.mca.parser.model.HeaderFieldSpec("exact", 5),    // 정확한 값
            new com.mca.parser.model.HeaderFieldSpec("long", 3)      // 긴 값
        ));
        config.setHeaderColumnCount(3);
        parser = new McaMessageParser(config);

        String rawLog = ":|ABC|12345|TOOLONG[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.headerFields().get("short")).isEqualTo("ABC       ");  // 10자리 패딩
        assertThat(result.headerFields().get("exact")).isEqualTo("12345");       // 5자리 그대로
        assertThat(result.headerFields().get("long")).isEqualTo("TOO");          // 3자리로 자름
    }

    @Test
    @DisplayName("빈 필드 처리 - 연속된 구분자")
    void parse_빈필드_연속구분자() {
        // Given - "||01| |" 형식
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("field1", 10),
            new com.mca.parser.model.HeaderFieldSpec("field2", 10),
            new com.mca.parser.model.HeaderFieldSpec("field3", 10),
            new com.mca.parser.model.HeaderFieldSpec("field4", 10)
        ));
        config.setHeaderColumnCount(4);
        parser = new McaMessageParser(config);

        String rawLog = ":|first||01| |bodyData[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.headerFields()).hasSize(4);
        assertThat(result.headerFields().get("field1")).isEqualTo("first     ");  // "first" → 10자
        assertThat(result.headerFields().get("field2")).isEqualTo("          ");  // "" → 10자 공백
        assertThat(result.headerFields().get("field3")).isEqualTo("01        ");  // "01" → 10자
        assertThat(result.headerFields().get("field4")).isEqualTo("          ");  // " " → 10자 (공백 1개 + 9개)

        // header 문자열도 확인 (delimiter 제거)
        assertThat(result.header()).hasSize(40);  // 10*4
        assertThat(result.body()).isEqualTo("bodyData");
    }

    @Test
    @DisplayName("빈 필드 처리 - 시작부터 빈값")
    void parse_빈필드_시작부터() {
        // Given - "|value|" 형식
        config.setHeaderFieldSpecs(List.of(
            new com.mca.parser.model.HeaderFieldSpec("field1", 5),
            new com.mca.parser.model.HeaderFieldSpec("field2", 5),
            new com.mca.parser.model.HeaderFieldSpec("field3", 5)
        ));
        config.setHeaderColumnCount(3);
        parser = new McaMessageParser(config);

        String rawLog = ":||value|[EXT]";

        // When
        McaMessage result = parser.parse(rawLog);

        // Then
        assertThat(result.headerFields()).hasSize(3);
        assertThat(result.headerFields().get("field1")).isEqualTo("     ");   // "" → 5자 공백
        assertThat(result.headerFields().get("field2")).isEqualTo("value");   // "value" → 5자 그대로
        assertThat(result.headerFields().get("field3")).isEqualTo("     ");   // "" → 5자 공백
    }
}
