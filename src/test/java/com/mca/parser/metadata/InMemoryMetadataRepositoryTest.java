package com.mca.parser.metadata;

import com.mca.parser.model.HeaderFieldSpec;
import com.mca.parser.model.InRecSpec;
import com.mca.parser.model.RecordFieldSpec;
import com.mca.parser.model.ServiceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Caffeine 캐시 기반 InMemoryMetadataRepository 테스트")
class InMemoryMetadataRepositoryTest {

    private InMemoryMetadataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMetadataRepository();
    }

    @Test
    @DisplayName("메타데이터 저장 및 조회")
    void testSaveAndFind() {
        // Given
        ServiceMetadata metadata = createSampleMetadata("test001");

        // When
        repository.save(metadata);
        Optional<ServiceMetadata> found = repository.findByInterfaceName("test001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getInterfaceName()).isEqualTo("test001");
        assertThat(found.get().getHeaderFields()).hasSize(2);
        assertThat(found.get().getInRecs()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 메타데이터 조회")
    void testFindNonExistent() {
        // When
        Optional<ServiceMetadata> found = repository.findByInterfaceName("unknown");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("캐시 덮어쓰기")
    void testOverwrite() {
        // Given
        ServiceMetadata metadata1 = createSampleMetadata("test001");

        ServiceMetadata metadata2 = new ServiceMetadata();
        metadata2.setInterfaceName("test001");
        metadata2.setHeaderFields(List.of(
                new HeaderFieldSpec("field1", 10),
                new HeaderFieldSpec("field2", 20),
                new HeaderFieldSpec("extraField", 30)
        ));
        metadata2.setInRecs(List.of());

        // When
        repository.save(metadata1);
        repository.save(metadata2);  // 덮어쓰기
        Optional<ServiceMetadata> found = repository.findByInterfaceName("test001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getHeaderFields()).hasSize(3);  // 3개로 증가
    }

    @Test
    @DisplayName("캐시 전체 삭제")
    void testClear() {
        // Given
        repository.save(createSampleMetadata("test001"));
        repository.save(createSampleMetadata("test002"));
        repository.save(createSampleMetadata("test003"));

        // When
        repository.clear();

        // Then
        assertThat(repository.findByInterfaceName("test001")).isEmpty();
        assertThat(repository.findByInterfaceName("test002")).isEmpty();
        assertThat(repository.findByInterfaceName("test003")).isEmpty();
    }

    @Test
    @DisplayName("캐시 통계 확인")
    void testCacheStats() {
        // Given
        repository.save(createSampleMetadata("test001"));

        // When
        repository.findByInterfaceName("test001");  // Hit
        repository.findByInterfaceName("test001");  // Hit
        repository.findByInterfaceName("unknown");  // Miss

        String stats = repository.getCacheStats();

        // Then
        assertThat(stats).contains("HitRate");
        assertThat(stats).contains("MissRate");
        assertThat(stats).contains("Size");
        System.out.println(stats);
    }

    @Test
    @DisplayName("여러 메타데이터 저장 및 조회")
    void testMultipleMetadata() {
        // Given
        for (int i = 1; i <= 10; i++) {
            ServiceMetadata metadata = createSampleMetadata("test" + String.format("%03d", i));
            repository.save(metadata);
        }

        // When & Then
        for (int i = 1; i <= 10; i++) {
            String interfaceName = "test" + String.format("%03d", i);
            Optional<ServiceMetadata> found = repository.findByInterfaceName(interfaceName);

            assertThat(found).isPresent();
            assertThat(found.get().getInterfaceName()).isEqualTo(interfaceName);
        }

        // 통계 출력
        System.out.println(repository.getCacheStats());
    }

    /**
     * 테스트용 샘플 메타데이터 생성
     */
    private ServiceMetadata createSampleMetadata(String interfaceName) {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setInterfaceName(interfaceName);

        // Header 필드
        metadata.setHeaderFields(List.of(
                new HeaderFieldSpec("field1", 10),
                new HeaderFieldSpec("field2", 20)
        ));

        // InRec1
        InRecSpec inRec1 = new InRecSpec();
        inRec1.setName("InRec1");
        inRec1.setFields(List.of(
                new RecordFieldSpec("DATA1", 8),
                new RecordFieldSpec("DATA2", 10)
        ));

        metadata.setInRecs(List.of(inRec1));

        return metadata;
    }
}
