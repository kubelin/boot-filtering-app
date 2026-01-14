package com.mca.parser.metadata;

import com.mca.parser.model.ServiceMetadata;

import java.util.Optional;

/**
 * 메타데이터 저장소 인터페이스
 * - 실제 구현은 DB 또는 메모리 기반
 */
public interface MetadataRepository {

    /**
     * 서비스 인터페이스 이름으로 메타데이터 조회
     *
     * @param interfaceName 서비스 인터페이스 이름 (예: "eab0001p0")
     * @return 서비스 메타데이터
     */
    Optional<ServiceMetadata> findByInterfaceName(String interfaceName);

    /**
     * 메타데이터 저장
     *
     * @param metadata 서비스 메타데이터
     */
    void save(ServiceMetadata metadata);
}
