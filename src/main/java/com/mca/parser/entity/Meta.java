package com.mca.parser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Meta 테이블 엔티티
 * - 서비스별 메타데이터 정의
 */
@Entity
@Table(name = "meta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meta {

    /**
     * 서비스 인터페이스 이름 (PK)
     * 예: "eab0001p0"
     */
    @Id
    @Column(name = "interface_name", length = 50, nullable = false)
    private String interfaceName;

    /**
     * 헤더 필드 정의 (JSON 형식)
     * 예: [{"name":"sendTimeStamp","length":17},{"name":"userId","length":10}]
     *
     * 필요시 별도 테이블로 분리 가능
     */
    @Column(name = "header_fields", columnDefinition = "TEXT")
    private String headerFields;

    /**
     * 연관된 InRec 필드 정의들
     */
    @OneToMany(mappedBy = "meta", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("inRecName ASC, fieldOrder ASC")
    private List<MetaDetail> metaDetails = new ArrayList<>();
}
