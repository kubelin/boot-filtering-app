-- ============================================
-- MCA Parser Metadata Tables DDL
-- Database: MariaDB / MySQL
-- ============================================

-- 1. Meta 테이블 (서비스별 메타데이터)
CREATE TABLE IF NOT EXISTS meta (
    interface_name VARCHAR(50) PRIMARY KEY COMMENT '서비스 인터페이스 이름 (예: eab0001p0)',
    header_fields TEXT COMMENT '헤더 필드 정의 (JSON 형식)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='서비스별 메타데이터 정의';

-- 2. MetaDetail 테이블 (InRec 필드 정의)
CREATE TABLE IF NOT EXISTS meta_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본키',
    interface_name VARCHAR(50) NOT NULL COMMENT '서비스 인터페이스 이름 (FK)',
    in_rec_name VARCHAR(50) NOT NULL COMMENT 'InRec 이름 (예: InRec1, InRec2)',
    field_name VARCHAR(100) NOT NULL COMMENT '필드 이름 (예: ORD_DT, STCK_CODE)',
    field_length INT NOT NULL COMMENT '필드 길이',
    field_order INT NOT NULL COMMENT '필드 순서 (InRec 내에서의 순서)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',

    FOREIGN KEY (interface_name) REFERENCES meta(interface_name) ON DELETE CASCADE,
    INDEX idx_interface_name (interface_name),
    INDEX idx_in_rec_name (in_rec_name),
    INDEX idx_order (interface_name, in_rec_name, field_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='InRec 필드 정의';

-- ============================================
-- 샘플 데이터 (eab0001p0 서비스)
-- ============================================

-- Meta 데이터 삽입
INSERT INTO meta (interface_name, header_fields) VALUES
('eab0001p0', '[{"name":"sendTimeStamp","length":17},{"name":"userId","length":10},{"name":"interface","length":20},{"name":"uuid","length":36}]');

-- MetaDetail 데이터 삽입 (InRec1)
INSERT INTO meta_detail (interface_name, in_rec_name, field_name, field_length, field_order) VALUES
('eab0001p0', 'InRec1', 'ORD_DT', 8, 1),
('eab0001p0', 'InRec1', 'ORD_GBN', 2, 2),
('eab0001p0', 'InRec1', 'ACTV_YN', 1, 3);

-- MetaDetail 데이터 삽입 (InRec2)
INSERT INTO meta_detail (interface_name, in_rec_name, field_name, field_length, field_order) VALUES
('eab0001p0', 'InRec2', 'STCK_CODE', 6, 4),
('eab0001p0', 'InRec2', 'ORD_QTY', 10, 5),
('eab0001p0', 'InRec2', 'ORD_UNPR', 10, 6);

-- ============================================
-- 조회 쿼리 예시
-- ============================================

-- 1. 특정 서비스의 전체 메타데이터 조회
SELECT
    m.interface_name,
    m.header_fields,
    md.in_rec_name,
    md.field_name,
    md.field_length,
    md.field_order
FROM meta m
LEFT JOIN meta_detail md ON m.interface_name = md.interface_name
WHERE m.interface_name = 'eab0001p0'
ORDER BY md.in_rec_name, md.field_order;

-- 2. InRec별 필드 개수 확인
SELECT
    interface_name,
    in_rec_name,
    COUNT(*) as field_count,
    SUM(field_length) as total_length
FROM meta_detail
GROUP BY interface_name, in_rec_name
ORDER BY interface_name, in_rec_name;
