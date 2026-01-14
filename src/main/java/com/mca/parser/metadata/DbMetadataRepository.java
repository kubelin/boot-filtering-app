package com.mca.parser.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.parser.entity.Meta;
import com.mca.parser.entity.MetaDetail;
import com.mca.parser.model.HeaderFieldSpec;
import com.mca.parser.model.InRecSpec;
import com.mca.parser.model.RecordFieldSpec;
import com.mca.parser.model.ServiceMetadata;
import com.mca.parser.repository.MetaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DB 기반 메타데이터 저장소
 * - meta, meta_detail 테이블에서 메타데이터 조회
 * - JSON 파싱을 통한 헤더 필드 변환
 *
 * 사용 방법:
 * - application.yml에서 spring.profiles.active=db 설정
 * - 또는 @Qualifier("dbMetadataRepository")로 명시적 주입
 */
@Slf4j
@Repository("dbMetadataRepository")
@RequiredArgsConstructor
public class DbMetadataRepository implements MetadataRepository {

    private final MetaJpaRepository metaJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ServiceMetadata> findByInterfaceName(String interfaceName) {
        log.debug("DB에서 메타데이터 조회: interfaceName={}", interfaceName);

        return metaJpaRepository.findById(interfaceName)
                .map(this::convertToServiceMetadata);
    }

    @Override
    public void save(ServiceMetadata metadata) {
        log.debug("DB에 메타데이터 저장: interfaceName={}", metadata.getInterfaceName());

        Meta meta = convertToEntity(metadata);
        metaJpaRepository.save(meta);
    }

    /**
     * Entity → ServiceMetadata 변환
     */
    private ServiceMetadata convertToServiceMetadata(Meta meta) {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setInterfaceName(meta.getInterfaceName());

        // 1. Header 필드 파싱 (JSON → List<HeaderFieldSpec>)
        List<HeaderFieldSpec> headerFields = parseHeaderFields(meta.getHeaderFields());
        serviceMetadata.setHeaderFields(headerFields);

        // 2. InRec 필드 파싱 (MetaDetail → List<InRecSpec>)
        List<InRecSpec> inRecs = parseInRecs(meta.getMetaDetails());
        serviceMetadata.setInRecs(inRecs);

        return serviceMetadata;
    }

    /**
     * JSON 문자열 → List<HeaderFieldSpec> 파싱
     * 예: [{"name":"sendTimeStamp","length":17},{"name":"userId","length":10}]
     */
    private List<HeaderFieldSpec> parseHeaderFields(String headerFieldsJson) {
        if (headerFieldsJson == null || headerFieldsJson.trim().isEmpty()) {
            log.warn("headerFields가 비어있습니다. 빈 리스트 반환");
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    headerFieldsJson,
                    new TypeReference<List<HeaderFieldSpec>>() {}
            );
        } catch (Exception e) {
            log.error("headerFields JSON 파싱 실패: {}", headerFieldsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * List<MetaDetail> → List<InRecSpec> 변환
     * - MetaDetail을 InRec 이름으로 그룹화
     * - 각 그룹을 InRecSpec으로 변환
     */
    private List<InRecSpec> parseInRecs(List<MetaDetail> metaDetails) {
        if (metaDetails == null || metaDetails.isEmpty()) {
            log.warn("metaDetails가 비어있습니다. 빈 리스트 반환");
            return Collections.emptyList();
        }

        // InRec 이름으로 그룹화
        Map<String, List<MetaDetail>> groupedByInRec = metaDetails.stream()
                .collect(Collectors.groupingBy(
                        MetaDetail::getInRecName,
                        LinkedHashMap::new,  // 순서 유지
                        Collectors.toList()
                ));

        // 각 그룹을 InRecSpec으로 변환
        return groupedByInRec.entrySet().stream()
                .map(entry -> {
                    String inRecName = entry.getKey();
                    List<MetaDetail> details = entry.getValue();

                    // field_order 순서로 정렬
                    details.sort(Comparator.comparing(MetaDetail::getFieldOrder));

                    // RecordFieldSpec 리스트 생성
                    List<RecordFieldSpec> fields = details.stream()
                            .map(detail -> new RecordFieldSpec(
                                    detail.getFieldName(),
                                    detail.getFieldLength()
                            ))
                            .collect(Collectors.toList());

                    InRecSpec inRecSpec = new InRecSpec();
                    inRecSpec.setName(inRecName);
                    inRecSpec.setFields(fields);

                    return inRecSpec;
                })
                .collect(Collectors.toList());
    }

    /**
     * ServiceMetadata → Entity 변환 (save 시 사용)
     */
    private Meta convertToEntity(ServiceMetadata metadata) {
        Meta meta = new Meta();
        meta.setInterfaceName(metadata.getInterfaceName());

        // 1. Header 필드를 JSON 문자열로 변환
        try {
            String headerFieldsJson = objectMapper.writeValueAsString(metadata.getHeaderFields());
            meta.setHeaderFields(headerFieldsJson);
        } catch (Exception e) {
            log.error("headerFields JSON 변환 실패", e);
            throw new RuntimeException("headerFields JSON 변환 실패", e);
        }

        // 2. InRec들을 MetaDetail 엔티티로 변환
        List<MetaDetail> metaDetails = new ArrayList<>();
        int fieldOrder = 1;

        for (InRecSpec inRec : metadata.getInRecs()) {
            for (RecordFieldSpec field : inRec.getFields()) {
                MetaDetail detail = new MetaDetail();
                detail.setMeta(meta);
                detail.setInRecName(inRec.getName());
                detail.setFieldName(field.getName());
                detail.setFieldLength(field.getLength());
                detail.setFieldOrder(fieldOrder++);

                metaDetails.add(detail);
            }
        }

        meta.setMetaDetails(metaDetails);

        return meta;
    }
}
