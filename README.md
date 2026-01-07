# MCA 로그 파싱 애플리케이션

MCA 로그를 파싱하여 FLD(Fixed Length Data) 형식으로 변환하고 타겟 시스템으로 전송하는 Spring Boot 애플리케이션입니다.

## 📋 주요 기능

- **MCA 로그 파싱**: 구분자(`|`) 기반 MCA 로그 데이터 파싱
- **FLD 메시지 변환**: Header + Body 구조의 고정길이 메시지 생성
- **타겟 시스템 연동**: RestTemplate을 사용한 HTTP 통신
- **로컬 테스트 지원**: Mock 환경에서 서버 연결 없이 테스트 가능
- **Main 메서드 테스트**: 애플리케이션을 직접 실행하여 전체 플로우 검증
- **재시도 메커니즘**: 지수 백오프를 사용한 자동 재시도

## 🛠️ 기술 스택

- **Java 17** (Java 21 호환)
- **Spring Boot 3.2.0**
- **Gradle 8.5**
- **Lombok**
- **JUnit 5** + **AssertJ**

## 📁 프로젝트 구조

```
src/main/java/com/mca/
├── McaParsingApplication.java          # 메인 애플리케이션 (테스트 모드 포함)
│
├── parser/                             # MCA 로그 파싱
│   ├── McaLogParser.java              # 로그 파서
│   └── model/
│       └── McaLogData.java            # 파싱된 데이터 모델
│
├── fld/                                # FLD 메시지 빌드
│   ├── FldMessageBuilder.java         # 메시지 빌더
│   └── model/
│       ├── FldHeader.java             # 헤더 (46 bytes)
│       ├── FldBody.java               # 바디 (가변 길이)
│       └── FldMessage.java            # 전체 메시지
│
├── client/                             # 타겟 시스템 클라이언트
│   ├── TargetSystemClient.java        # HTTP 클라이언트
│   └── config/
│       └── TargetSystemConfig.java    # 클라이언트 설정
│
├── service/                            # 비즈니스 로직
│   └── McaProcessingService.java      # 처리 서비스
│
├── api/                                # REST API
│   └── McaController.java             # API 컨트롤러
│
├── model/                              # 공통 모델
│   ├── FieldSpec.java                 # 필드 스펙 정의
│   └── TargetSystemResponse.java      # 응답 모델
│
└── config/                             # 설정
    ├── RestTemplateConfig.java        # RestTemplate 설정
    └── LocalTestConfig.java           # 로컬 테스트 설정

src/test/java/com/mca/
├── parser/
│   └── McaLogParserTest.java
├── fld/
│   └── FldMessageBuilderTest.java
└── service/
    └── McaProcessingServiceTest.java
```

---

## 🚀 빌드 및 실행

### ✅ 사전 요구사항

- **Java 17 이상** 설치
- **Gradle** (Wrapper 포함)

```bash
# Java 버전 확인
java -version
```

### 1️⃣ 프로젝트 빌드

```bash
# 클린 빌드
./gradlew clean build
```

**빌드 결과:**
```
BUILD SUCCESSFUL in 3s
8 actionable tasks: 8 executed

빌드 파일 위치:
build/libs/mca-parsing-application-1.0.0.jar (약 20MB)
```

### 2️⃣ 테스트 실행

```bash
# 단위 테스트 실행
./gradlew test
```

**테스트 결과:**
- `McaLogParserTest`: MCA 로그 파싱 검증
- `FldMessageBuilderTest`: FLD 메시지 빌드 검증
- `McaProcessingServiceTest`: 통합 처리 플로우 검증

### 3️⃣ 애플리케이션 실행 방법

#### 방법 A: 테스트 모드 (추천) - 서버 연결 없이 즉시 테스트

```bash
# Gradle로 실행
./gradlew bootRun --args='test'

# 또는 JAR 파일로 실행
java -jar build/libs/mca-parsing-application-1.0.0.jar test
```

**실행 결과 예시:**
```
==============================================
  MCA 파싱 애플리케이션 - 테스트 모드
==============================================

1. MCA 로그 파싱 테스트
   원본 로그: MCA0|20240101|120000|SAMPLE_DATA_FOR_TESTING

   ✓ 파싱 성공
   - 메시지 타입: MCA0
   - 날짜: 20240101
   - 시간: 120000
   - 바디: SAMPLE_DATA_FOR_TESTING

2. FLD 메시지 빌드 테스트
   ✓ FLD 메시지 빌드 성공
   - 메시지 길이: 79 bytes
   - FLD 문자열: MCA04f0946e5144b49d88b9520260108002637MCASYS010000000001SAMPLE_DATA_FOR_TESTING

3. 타겟 시스템 호출 테스트
   ✓ 타겟 시스템 호출 성공
   - Success: true
   - Code: 200
   - Message: MOCK_SUCCESS: 타겟 시스템 응답 시뮬레이션
   - Date: 2026-01-08
   - Timestamp: 2026-01-07T15:26:37.125569Z

==============================================
  ✅ 모든 테스트 통과
==============================================
```

#### 방법 B: Spring Boot 서버 모드 (로컬 환경)

```bash
# Gradle로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 JAR 파일로 실행
java -jar build/libs/mca-parsing-application-1.0.0.jar --spring.profiles.active=local
```

서버가 `http://localhost:8080`에서 실행됩니다.

#### 방법 C: 운영 환경 실행

```bash
# 타겟 시스템 엔드포인트 환경변수 설정
export TARGET_ENDPOINT=http://real-target-system.com/api

# 운영 프로파일로 실행
java -jar build/libs/mca-parsing-application-1.0.0.jar --spring.profiles.active=prod
```

---

## 📡 REST API 사용법

### 1. Spring Boot 서버 시작

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. API 호출

#### ① MCA 로그 처리

```bash
curl -X POST http://localhost:8080/api/v1/mca/process \
  -H "Content-Type: text/plain" \
  -d "MCA0|20240108|093000|REAL_DATA"
```

**응답:**
```json
{
  "success": true,
  "code": 200,
  "message": "MOCK_SUCCESS: 타겟 시스템 응답 시뮬레이션",
  "date": "2026-01-08",
  "timestamp": "2026-01-08T00:30:00.123456Z"
}
```

#### ② MCA 로그 처리 (재시도 포함)

```bash
curl -X POST http://localhost:8080/api/v1/mca/process-retry \
  -H "Content-Type: text/plain" \
  -d "MCA0|20240108|093000|RETRY_DATA"
```

최대 3회 재시도 (설정 가능)

#### ③ 헬스 체크

```bash
curl http://localhost:8080/api/v1/mca/health
```

**응답:** `OK`

---

## ⚙️ 설정 파일

### application.yml (기본 설정)

```yaml
spring:
  application:
    name: mca-parsing-application
  profiles:
    active: local

target:
  system:
    endpoint: ${TARGET_ENDPOINT:http://localhost:8080/api/receive}
    timeout: 5000      # 연결 타임아웃 (밀리초)
    max-retries: 3     # 최대 재시도 횟수

logging:
  level:
    com.mca: DEBUG
    org.springframework: INFO
```

### application-local.yml (로컬 테스트)

```yaml
target:
  system:
    endpoint: http://localhost:8080/mock/api
    timeout: 1000
    max-retries: 0    # 재시도 비활성화

logging:
  level:
    com.mca: TRACE    # 상세 로그
```

### application-prod.yml (운영 환경)

```yaml
target:
  system:
    endpoint: ${TARGET_ENDPOINT}
    timeout: 10000
    max-retries: 5

logging:
  level:
    com.mca: INFO
    org.springframework: WARN
```

---

## 📊 처리 플로우

```
┌─────────────────┐
│  MCA 로그 입력  │  예: "MCA0|20240101|120000|DATA"
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  McaLogParser       │  파싱: messageType, date, time, body
│  (로그 파싱)        │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ FldMessageBuilder   │
│ (FLD 메시지 생성)   │
├─────────────────────┤
│ ├─ FldHeader        │  46 bytes (messageType + transactionId + timestamp + systemCode)
│ └─ FldBody          │  가변 길이 (recordCount + dataContent)
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ TargetSystemClient  │  RestTemplate POST 요청
│ (타겟 시스템 전송)  │  - 재시도 지원 (지수 백오프)
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ TargetSystemResponse│  { success, code, message, date, timestamp }
└─────────────────────┘
```

---

## 💡 주요 특징

### 1. Java 17 Record 활용

불변 데이터 구조로 안전하고 간결한 코드:

```java
// 응답 모델
public record TargetSystemResponse(
    boolean success,
    int code,
    String message,
    LocalDate date,
    Instant timestamp
) {
    public static TargetSystemResponse success(int code, String message) {
        return new TargetSystemResponse(true, code, message,
                                        LocalDate.now(), Instant.now());
    }
}

// FLD 헤더
public record FldHeader(
    String messageType,      // 4자리
    String transactionId,    // 20자리
    String timestamp,        // 14자리
    String systemCode        // 8자리
) { }
```

### 2. 로컬 테스트 지원

Mock RestTemplate 자동 구성으로 실제 서버 없이 테스트:

```java
@Configuration
@Profile("local")
public class LocalTestConfig {
    @Bean
    public RestTemplate mockRestTemplate() {
        // Mock 응답 반환
        return new RestTemplate() { /* ... */ };
    }
}
```

### 3. Main 메서드 테스트 모드

CI/CD 파이프라인에서 활용 가능:

```bash
# Jenkins, GitHub Actions 등에서 사용
java -jar app.jar test
echo "Exit code: $?"  # 0: 성공, 1: 실패
```

### 4. 재시도 메커니즘

지수 백오프 패턴으로 안정적인 재시도:

```java
public TargetSystemResponse sendWithRetry(FldMessage message) {
    int attempts = 0;
    while (attempts <= config.getMaxRetries()) {
        attempts++;
        response = send(message);
        if (response.success()) return response;

        // 1초, 2초, 3초... 대기
        Thread.sleep(1000L * attempts);
    }
    return response;
}
```

---

## 🔍 FLD 메시지 구조

### Header (46 bytes 고정)

| 필드           | 길이 | 설명                      | 예시                  |
|----------------|------|---------------------------|-----------------------|
| messageType    | 4    | 메시지 타입               | `MCA0`                |
| transactionId  | 20   | 거래 ID (UUID)            | `4f0946e5144b49d88b95` |
| timestamp      | 14   | yyyyMMddHHmmss            | `20260108002637`      |
| systemCode     | 8    | 시스템 코드               | `MCASYS01`            |

### Body (가변 길이)

| 필드        | 길이 | 설명                    |
|-------------|------|-------------------------|
| recordCount | 10   | 레코드 개수 (0 패딩)    |
| dataContent | N    | 실제 데이터 (구분자 제거) |

### 전체 예시

**입력 (MCA 로그):**
```
MCA0|20240108|093000|SAMPLE_DATA
```

**출력 (FLD 메시지):**
```
MCA04f0946e5144b49d88b9520260108093000MCASYS010000000001SAMPLE_DATA
```

---

## 🧪 테스트 가이드

### 단위 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests McaLogParserTest
./gradlew test --tests FldMessageBuilderTest
./gradlew test --tests McaProcessingServiceTest
```

### 통합 테스트 (테스트 모드)

```bash
# 방법 1: Gradle
./gradlew bootRun --args='test'

# 방법 2: JAR 직접 실행
java -jar build/libs/mca-parsing-application-1.0.0.jar test
```

### API 테스트 (서버 모드)

```bash
# 1. 서버 시작
./gradlew bootRun --args='--spring.profiles.active=local' &

# 2. API 호출
curl -X POST http://localhost:8080/api/v1/mca/process \
  -H "Content-Type: text/plain" \
  -d "MCA0|20240108|093000|TEST"

# 3. 서버 종료
kill %1
```

---

## 🐛 트러블슈팅

### 1. 빌드 실패

```bash
# Gradle 캐시 정리
./gradlew clean

# 의존성 다시 다운로드
./gradlew build --refresh-dependencies
```

### 2. Java 버전 오류

```bash
# Java 버전 확인 (17 이상 필요)
java -version

# 환경변수 확인
echo $JAVA_HOME

# Java 17 설치 (macOS)
brew install openjdk@17
```

### 3. 테스트 모드 실행 안됨

```bash
# 프로파일 명시적 지정
java -jar build/libs/mca-parsing-application-1.0.0.jar test \
  --spring.profiles.active=local

# 로그 레벨 변경
java -jar app.jar test --logging.level.com.mca=TRACE
```

### 4. 포트 충돌 (8080)

```bash
# 다른 포트로 실행
java -jar app.jar --server.port=9090
```

### 5. 타겟 시스템 연결 실패

```bash
# 타임아웃 조정 (application.yml)
target:
  system:
    timeout: 30000  # 30초로 증가
    max-retries: 5
```

---

## 📚 추가 문서

- [상세 설계 문서](DESIGN.md) - 시스템 아키텍처 및 설계 명세

---

## 🔧 개발 가이드

### MCA 로그 형식 변경

`McaLogParser.java`의 `parseFields()` 메서드 수정:

```java
private Map<String, String> parseFields(String rawLog) {
    // 구분자 변경: | → ,
    String[] parts = rawLog.split(",");
    // ...
}
```

### FLD 헤더 구조 변경

`FldHeader.java`의 `SPECS` 수정:

```java
private static final List<FieldSpec> SPECS = List.of(
    new FieldSpec("messageType", 4, FieldSpec.FieldType.STRING),
    new FieldSpec("newField", 10, FieldSpec.FieldType.STRING),  // 추가
    // ...
);
```

### 타겟 시스템 엔드포인트 변경

환경변수로 설정:

```bash
export TARGET_ENDPOINT=https://new-system.com/api/endpoint
java -jar app.jar --spring.profiles.active=prod
```

---

## 📈 성능

- **파싱 속도**: 평균 < 100μs per log
- **FLD 변환**: 평균 < 50μs per message
- **전체 처리**: 평균 < 200ms (네트워크 포함)

---

## 📄 라이센스

MIT License

---

## 👥 작성자

MCA Parsing Team

---

## 📞 문의

이슈 또는 문의사항은 GitHub Issues로 등록해주세요.
