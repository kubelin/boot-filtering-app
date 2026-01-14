package com.mca.parser.metadata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mca.parser.model.ServiceMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 캐시 기반 메타데이터 저장소
 * - 테스트 및 간단한 용도
 * - 실제 운영에서는 DB 기반 구현체 사용
 *
 * Caffeine 캐시 특징:
 * - 최대 1000개 엔트리 저장
 * - 1시간 후 자동 만료
 * - 히트율/미스율 통계 수집
 * - LRU 기반 자동 eviction
 *
 * 기본 구현체로 설정되어 있음 (테스트용)
 * DB 사용 시 FldToJsonConverter에서 @Qualifier("dbMetadataRepository") 사용
 */
@Slf4j
@Repository
@org.springframework.context.annotation.Primary
public class InMemoryMetadataRepository implements MetadataRepository {

    private final Cache<String, ServiceMetadata> cache;

    public InMemoryMetadataRepository() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)  // 최대 1000개 메타데이터
                .expireAfterWrite(1, TimeUnit.HOURS)  // 1시간 후 만료
                .recordStats()  // 통계 수집 (히트율, 미스율)
                .evictionListener((key, value, cause) -> {
                    log.debug("캐시 제거: key={}, cause={}", key, cause);
                })
                .build();

        log.info("Caffeine 캐시 초기화: maxSize=1000, expireAfterWrite=1h");
    }

    @Override
    public Optional<ServiceMetadata> findByInterfaceName(String interfaceName) {
        ServiceMetadata metadata = cache.getIfPresent(interfaceName);

        if (metadata != null) {
            log.debug("캐시 히트: interfaceName={}", interfaceName);
        } else {
            log.debug("캐시 미스: interfaceName={}", interfaceName);
        }

        return Optional.ofNullable(metadata);
    }

    @Override
    public void save(ServiceMetadata metadata) {
        cache.put(metadata.getInterfaceName(), metadata);
        log.debug("캐시 저장: interfaceName={}", metadata.getInterfaceName());
    }

    /**
     * 전체 데이터 삭제 (테스트용)
     */
    public void clear() {
        cache.invalidateAll();
        log.debug("캐시 전체 삭제");
    }

    /**
     * 캐시 통계 정보 조회
     */
    public String getCacheStats() {
        var stats = cache.stats();
        return String.format(
                "Cache Stats - HitRate: %.2f%%, MissRate: %.2f%%, Size: %d",
                stats.hitRate() * 100,
                stats.missRate() * 100,
                cache.estimatedSize()
        );
    }
}
