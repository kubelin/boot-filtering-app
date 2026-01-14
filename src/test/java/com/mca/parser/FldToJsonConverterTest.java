package com.mca.parser;

import com.mca.parser.metadata.InMemoryMetadataRepository;
import com.mca.parser.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("FLD → JSON 변환기 테스트")
class FldToJsonConverterTest {

    @Autowired
    private FldToJsonConverter converter;

    @Autowired
    private InMemoryMetadataRepository repository;

    @BeforeEach
    void setUp() {
        repository.clear();

        // 메타데이터 설정: eab0001p0 서비스
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setInterfaceName("eab0001p0");

        // Header 필드 정의
        metadata.setHeaderFields(List.of(
                new HeaderFieldSpec("sendTimeStamp", 17),
                new HeaderFieldSpec("userId", 10),
                new HeaderFieldSpec("interface", 20),
                new HeaderFieldSpec("uuid", 36)
        ));

        // InRec1 정의
        InRecSpec inRec1 = new InRecSpec();
        inRec1.setName("InRec1");
        inRec1.setFields(List.of(
                new RecordFieldSpec("ORD_DT", 8),
                new RecordFieldSpec("ORD_GBN", 2),
                new RecordFieldSpec("ACTV_YN", 1)
        ));

        // InRec2 정의
        InRecSpec inRec2 = new InRecSpec();
        inRec2.setName("InRec2");
        inRec2.setFields(List.of(
                new RecordFieldSpec("STCK_CODE", 6),
                new RecordFieldSpec("ORD_QTY", 10),
                new RecordFieldSpec("ORD_UNPR", 10)
        ));

        metadata.setInRecs(List.of(inRec1, inRec2));

        // 저장
        repository.save(metadata);
    }

    @Test
    @DisplayName("FLD 데이터를 JSON으로 변환")
    void testConvertToJson() {
        // Given - FLD 형식 데이터
        // Header: 83자 (17+10+20+36)
        // InRec1: 11자 (8+2+1)
        // InRec2: 26자 (6+10+10)
        String fldData = "20251122 11:00:11TD2211    eab0001p0           253d1aa5-1059-486f-abfc-a1d9c06981f3" +  // Header
                         "20251122A Y" +  // InRec1
                         "005930" + "0000001000" + "0000050000";  // InRec2

        // When
        String json = converter.convertToJson(fldData, "eab0001p0");

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"sendTimeStamp\" : \"20251122 11:00:11\"");
        assertThat(json).contains("\"userId\" : \"TD2211\"");
        assertThat(json).contains("\"interface\" : \"eab0001p0\"");
        assertThat(json).contains("\"InRec1\"");
        assertThat(json).contains("\"ORD_DT\" : \"20251122\"");
        assertThat(json).contains("\"ORD_GBN\" : \"A\"");
        assertThat(json).contains("\"ACTV_YN\" : \"Y\"");
        assertThat(json).contains("\"InRec2\"");
        assertThat(json).contains("\"STCK_CODE\" : \"005930\"");
        assertThat(json).contains("\"ORD_QTY\" : \"0000001000\"");
        assertThat(json).contains("\"ORD_UNPR\" : \"0000050000\"");

        System.out.println("Generated JSON:");
        System.out.println(json);
    }

    @Test
    @DisplayName("빈 필드 처리")
    void testEmptyFields() {
        // Given - 빈 필드 포함
        String fldData = "                 " +  // sendTimeStamp (17자 공백)
                         "          " +  // userId (10자 공백)
                         "                    " +  // interface (20자 공백)
                         "                                    " +  // uuid (36자 공백)
                         "        " + "  " + " " +  // InRec1 (8+2+1 공백)
                         "      " + "          " + "          ";  // InRec2 (6+10+10 공백)

        // When
        String json = converter.convertToJson(fldData, "eab0001p0");

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"sendTimeStamp\" : \"\"");
        assertThat(json).contains("\"userId\" : \"\"");
        assertThat(json).contains("\"ORD_DT\" : \"\"");
        assertThat(json).contains("\"STCK_CODE\" : \"\"");
    }

    @Test
    @DisplayName("메타데이터 없음 예외")
    void testMetadataNotFound() {
        // Given
        String fldData = "testdata";

        // When & Then
        assertThatThrownBy(() -> converter.convertToJson(fldData, "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메타데이터를 찾을 수 없습니다");
    }
}
