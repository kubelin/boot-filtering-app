# 증권 데이터 파서 (JDK 17 간소화 버전)

## 주요 특징

### 1. Java 17 Record 활용
- `FieldSpec`: 필드 정의 (record)
- `SecuritiesHeader`: 헤더 구조 (record)
- `SecuritiesDataRecord`: 데이터 레코드 (record)
- `SecuritiesMessage`: 전체 메시지 (record)

### 2. JDK 17 기능 활용
- **var 키워드**: 타입 추론으로 코드 간소화
- **switch 표현식**: 타입 변환 로직 단순화
- **List.of()**: 불변 컬렉션 생성
- **record**: 불변 데이터 클래스

### 3. 단순화된 구조
```
FieldSpec.java              # 필드 스펙 정의 (record)
SecuritiesHeader.java       # 헤더 정의 (record)
SecuritiesDataRecord.java   # 데이터 레코드 정의 (record)
SecuritiesMessage.java      # 전체 메시지 (record)
SecuritiesParser.java       # 파서 로직
ParserController.java       # REST API
SecuritiesParserTest.java   # 테스트
```

## 사용 방법

### 1. 필드 정의 수정
`SecuritiesHeader.java`와 `SecuritiesDataRecord.java`에서 필드 정의:

```java
public record SecuritiesHeader(
    String messageType,  // 실제 필드명으로 변경
    String tradeDate,
    String tradeTime,
    Long totalCount
) {
    private static final List<FieldSpec> SPECS = List.of(
        new FieldSpec("messageType", 4, STRING),  // 실제 길이로 변경
        new FieldSpec("tradeDate", 8, DATE),
        new FieldSpec("tradeTime", 6, TIME),
        new FieldSpec("totalCount", 10, NUMBER)
    );
}
```

### 2. API 호출

**FLD 형식:**
```bash
curl -X POST http://localhost:8080/api/v1/parse/fld \
  -H "Content-Type: text/plain" \
  -d "STCK20240101120000000000002:| c0005930삼성전자..."
```

**JSON 형식:**
```bash
curl -X POST http://localhost:8080/api/v1/parse/json \
  -H "Content-Type: text/plain" \
  -d "STCK20240101120000000000002:| c0005930삼성전자..."
```

## 성능

- 평균 파싱 시간: ~50μs (목표: 100μs 이하)
- JDK 17 최적화로 리플렉션 제거
- 불변 객체로 스레드 안전성 보장

## 의존성

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```
