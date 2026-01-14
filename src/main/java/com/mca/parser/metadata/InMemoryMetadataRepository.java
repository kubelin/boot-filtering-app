package com.mca.parser.metadata;

import com.mca.parser.model.ServiceMetadata;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반 메타데이터 저장소
 * - 테스트 및 간단한 용도
 * - 실제 운영에서는 DB 기반 구현체 사용
 *
 * 기본 구현체로 설정되어 있음 (테스트용)
 * DB 사용 시 FldToJsonConverter에서 @Qualifier("dbMetadataRepository") 사용
 */
@Repository
@org.springframework.context.annotation.Primary
public class InMemoryMetadataRepository implements MetadataRepository {

    private final Map<String, ServiceMetadata> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<ServiceMetadata> findByInterfaceName(String interfaceName) {
        return Optional.ofNullable(storage.get(interfaceName));
    }

    @Override
    public void save(ServiceMetadata metadata) {
        storage.put(metadata.getInterfaceName(), metadata);
    }

    /**
     * 전체 데이터 삭제 (테스트용)
     */
    public void clear() {
        storage.clear();
    }
}
