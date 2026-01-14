package com.mca.parser.repository;

import com.mca.parser.entity.MetaDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MetaDetail 테이블 JPA Repository
 */
@Repository
public interface MetaDetailJpaRepository extends JpaRepository<MetaDetail, Long> {

    /**
     * 서비스 인터페이스명으로 모든 필드 정의 조회
     * (InRec 이름과 필드 순서로 정렬)
     */
    List<MetaDetail> findByMeta_InterfaceNameOrderByInRecNameAscFieldOrderAsc(String interfaceName);
}
