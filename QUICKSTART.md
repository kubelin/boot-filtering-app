# 빠른 시작 가이드

## 🚀 5분 안에 시작하기

### 1단계: 빌드

```bash
cd /Users/imsjoo/Workspace-backend/boot-filtering-app
./gradlew clean build
```

### 2단계: 테스트 모드로 실행 (추천)

```bash
# 방법 1: Gradle 사용
./gradlew bootRun --args='test'

# 방법 2: JAR 직접 실행
java -jar build/libs/mca-parsing-application-1.0.0.jar test
```

**예상 결과:**
```
✅ 모든 테스트 통과
```

---

## 📌 주요 실행 방법 3가지

### ① 테스트 모드 (서버 연결 불필요)
```bash
java -jar build/libs/mca-parsing-application-1.0.0.jar test
```

### ② 로컬 서버 모드 (Mock 타겟 시스템)
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버 실행 후:
```bash
curl -X POST http://localhost:8080/api/v1/mca/process \
  -H "Content-Type: text/plain" \
  -d "MCA0|20240108|093000|TEST_DATA"
```

### ③ 운영 환경 모드
```bash
export TARGET_ENDPOINT=http://real-system.com/api
java -jar build/libs/mca-parsing-application-1.0.0.jar --spring.profiles.active=prod
```

---

## 📝 입출력 예시

**입력 (MCA 로그):**
```
MCA0|20240108|093000|SAMPLE_DATA
```

**출력 (FLD 메시지):**
```
MCA04f0946e5144b49d88b9520260108093000MCASYS010000000001SAMPLE_DATA
```

**응답 (JSON):**
```json
{
  "success": true,
  "code": 200,
  "message": "SUCCESS",
  "date": "2024-01-08",
  "timestamp": "2024-01-08T09:30:00Z"
}
```

---

## 🔧 설정 변경

### 타겟 시스템 엔드포인트 변경
```bash
# 환경변수로 설정
export TARGET_ENDPOINT=https://new-system.com/api

# 또는 application.yml 수정
target:
  system:
    endpoint: https://new-system.com/api
```

### 타임아웃 조정
```yaml
# application.yml
target:
  system:
    timeout: 10000      # 10초
    max-retries: 5      # 최대 5회 재시도
```

---

## 🐛 문제 해결

### 빌드 실패 시
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Java 버전 확인
```bash
java -version
# Java 17 이상 필요
```

### 포트 충돌 시
```bash
java -jar app.jar --server.port=9090
```

---

자세한 내용은 [README.md](README.md)를 참조하세요.
