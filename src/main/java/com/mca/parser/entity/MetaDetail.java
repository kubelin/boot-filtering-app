package com.mca.parser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MetaDetail 테이블 엔티티
 * - InRec 내부 필드 정의
 */
@Entity
@Table(name = "meta_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDetail {

    /**
     * 기본키 (자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 연관된 Meta (서비스)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_name", nullable = false)
    private Meta meta;

    /**
     * InRec 이름
     * 예: "InRec1", "InRec2"
     */
    @Column(name = "in_rec_name", length = 50, nullable = false)
    private String inRecName;

    /**
     * 필드 이름
     * 예: "ORD_DT", "STCK_CODE"
     */
    @Column(name = "field_name", length = 100, nullable = false)
    private String fieldName;

    /**
     * 필드 길이
     * 예: 8, 10, 6
     */
    @Column(name = "field_length", nullable = false)
    private Integer fieldLength;

    /**
     * 필드 순서 (InRec 내에서의 순서)
     * 예: 1, 2, 3
     */
    @Column(name = "field_order", nullable = false)
    private Integer fieldOrder;
}
