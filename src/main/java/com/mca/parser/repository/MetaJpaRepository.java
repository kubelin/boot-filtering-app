package com.mca.parser.repository;

import com.mca.parser.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Meta 테이블 JPA Repository
 */
@Repository
public interface MetaJpaRepository extends JpaRepository<Meta, String> {
    // interfaceName으로 조회 (기본 제공: findById)
}
