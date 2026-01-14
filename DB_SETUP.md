# DB 기반 메타데이터 저장소 설정 가이드

## 1. 개요

FLD → JSON 변환 시스템은 두 가지 메타데이터 저장소를 지원합니다:

- **InMemoryMetadataRepository** (기본): 메모리 기반, 테스트용
- **DbMetadataRepository**: DB 기반, 운영 환경용

## 2. DB 테이블 구조

### 2.1 meta 테이블 (서비스 메타데이터)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| interface_name | VARCHAR(50) | 서비스 인터페이스 이름 (PK) |
| header_fields | TEXT | 헤더 필드 정의 (JSON 형식) |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

**header_fields JSON 형식:**
```json
[
  {"name":"sendTimeStamp","length":17},
  {"name":"userId","length":10},
  {"name":"interface","length":20},
  {"name":"uuid","length":36}
]
```

### 2.2 meta_detail 테이블 (InRec 필드 정의)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 기본키 (AUTO_INCREMENT) |
| interface_name | VARCHAR(50) | 서비스 인터페이스 이름 (FK) |
| in_rec_name | VARCHAR(50) | InRec 이름 (예: InRec1, InRec2) |
| field_name | VARCHAR(100) | 필드 이름 (예: ORD_DT) |
| field_length | INT | 필드 길이 |
| field_order | INT | 필드 순서 (InRec 내에서의 순서) |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

## 3. DB 설정

### 3.1 테이블 생성

```bash
# MariaDB 접속
mysql -h 172.30.1.120 -P 3306 -u commondb_admin -p commonDB

# DDL 실행
source src/main/resources/db/schema.sql
```

### 3.2 application.yml 설정 확인

```yaml
spring:
  datasource:
    url: jdbc:mariadb://172.30.1.120:3306/commonDB
    driver-class-name: org.mariadb.jdbc.Driver
    username: commondb_admin
    password: AdminPass123!

  jpa:
    hibernate:
      ddl-auto: none  # 운영 환경: none (수동 DDL 관리)
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MariaDBDialect
```

## 4. 사용 방법

### 4.1 기본 사용 (InMemory - 현재 설정)

```java
@Autowired
private FldToJsonConverter converter;  // InMemoryMetadataRepository 사용

// 테스트 코드에서 메타데이터 직접 설정
@BeforeEach
void setUp() {
    repository.clear();

    ServiceMetadata metadata = new ServiceMetadata();
    metadata.setInterfaceName("eab0001p0");
    // ... 필드 설정

    repository.save(metadata);
}
```

### 4.2 DB 사용 방식 1 - @Qualifier 사용

```java
@Service
public class MyService {

    private final FldToJsonConverter converter;

    @Autowired
    public MyService(
        ObjectMapper objectMapper,
        @Qualifier("dbMetadataRepository") MetadataRepository repository
    ) {
        this.converter = new FldToJsonConverter(repository, objectMapper);
    }
}
```

### 4.3 DB 사용 방식 2 - InMemoryMetadataRepository @Primary 제거

`InMemoryMetadataRepository.java`에서 `@Primary` 제거하고,
`DbMetadataRepository.java`에 `@Primary` 추가

```java
@Repository
@Primary  // DB를 기본으로 사용
public class DbMetadataRepository implements MetadataRepository {
    // ...
}
```

## 5. 샘플 데이터

### 5.1 서비스 등록 예시 (eab0001p0)

```sql
-- Meta 데이터
INSERT INTO meta (interface_name, header_fields) VALUES
('eab0001p0', '[{"name":"sendTimeStamp","length":17},{"name":"userId","length":10},{"name":"interface","length":20},{"name":"uuid","length":36}]');

-- InRec1 필드들
INSERT INTO meta_detail (interface_name, in_rec_name, field_name, field_length, field_order) VALUES
('eab0001p0', 'InRec1', 'ORD_DT', 8, 1),
('eab0001p0', 'InRec1', 'ORD_GBN', 2, 2),
('eab0001p0', 'InRec1', 'ACTV_YN', 1, 3);

-- InRec2 필드들
INSERT INTO meta_detail (interface_name, in_rec_name, field_name, field_length, field_order) VALUES
('eab0001p0', 'InRec2', 'STCK_CODE', 6, 4),
('eab0001p0', 'InRec2', 'ORD_QTY', 10, 5),
('eab0001p0', 'InRec2', 'ORD_UNPR', 10, 6);
```

### 5.2 FLD 데이터 변환 예시

```java
String fldData =
    "20251122 11:00:11TD2211    eab0001p0           253d1aa5-1059-486f-abfc-a1d9c06981f3" +  // Header (83자)
    "20251122A Y" +  // InRec1 (11자)
    "005930" + "0000001000" + "0000050000";  // InRec2 (26자)

String json = converter.convertToJson(fldData, "eab0001p0");
```

**결과:**
```json
{
  "header" : {
    "sendTimeStamp" : "20251122 11:00:11",
    "userId" : "TD2211",
    "interface" : "eab0001p0",
    "uuid" : "253d1aa5-1059-486f-abfc-a1d9c06981f3"
  },
  "data" : {
    "InRec1" : {
      "ORD_DT" : "20251122",
      "ORD_GBN" : "A",
      "ACTV_YN" : "Y"
    },
    "InRec2" : {
      "STCK_CODE" : "005930",
      "ORD_QTY" : "0000001000",
      "ORD_UNPR" : "0000050000"
    }
  }
}
```

## 6. 조회 쿼리

### 6.1 특정 서비스 메타데이터 전체 조회

```sql
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
```

### 6.2 InRec별 필드 개수 및 길이 확인

```sql
SELECT
    interface_name,
    in_rec_name,
    COUNT(*) as field_count,
    SUM(field_length) as total_length
FROM meta_detail
GROUP BY interface_name, in_rec_name
ORDER BY interface_name, in_rec_name;
```

## 7. 트러블슈팅

### 7.1 테이블이 없다는 오류

```
java.sql.SQLSyntaxErrorException: Table 'commonDB.meta' doesn't exist
```

**해결:** `src/main/resources/db/schema.sql` 실행

### 7.2 JSON 파싱 오류

```
headerFields JSON 파싱 실패
```

**해결:** header_fields 컬럼의 JSON 형식 확인

```sql
-- 올바른 형식
[{"name":"sendTimeStamp","length":17}]

-- 잘못된 형식
{"name":"sendTimeStamp","length":17}  -- 배열이 아님
```

### 7.3 필드 순서가 잘못됨

**해결:** field_order 값 확인 및 수정

```sql
UPDATE meta_detail
SET field_order = 1
WHERE interface_name = 'eab0001p0' AND in_rec_name = 'InRec1' AND field_name = 'ORD_DT';
```

## 8. 아키텍처

```
[FLD Data] → [FldToJsonConverter]
                    ↓
            [MetadataRepository Interface]
                    ↓
        ┌───────────┴───────────┐
        ↓                       ↓
[InMemoryMetadataRepository]  [DbMetadataRepository]
   (테스트용)                  (운영용)
                                ↓
                    ┌───────────┴───────────┐
                    ↓                       ↓
            [MetaJpaRepository]    [MetaDetailJpaRepository]
                    ↓                       ↓
                [meta 테이블]        [meta_detail 테이블]
```

## 9. 참고

- Entity 위치: `src/main/java/com/mca/parser/entity/`
- Repository 위치: `src/main/java/com/mca/parser/repository/`
- 구현체 위치: `src/main/java/com/mca/parser/metadata/`
- DDL 위치: `src/main/resources/db/schema.sql`
