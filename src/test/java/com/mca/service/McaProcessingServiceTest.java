package com.mca.service;

import com.mca.model.TargetSystemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local") // Mock RestTemplate 사용
class McaProcessingServiceTest {

    @Autowired
    private McaProcessingService service;

    private static final String SAMPLE_LOG = "metadata :|MCA0|20240101|120000|INTEGRATION_TEST[EXT]";

    @Test
    void testProcess() {
        // When
        TargetSystemResponse response = service.process(SAMPLE_LOG);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.message()).isNotNull();
        assertThat(response.date()).isNotNull();
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void testProcessInvalidLog() {
        // When
        TargetSystemResponse response = service.process("");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("오류");
    }
}
