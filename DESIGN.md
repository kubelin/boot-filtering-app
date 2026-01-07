# MCA Log Parsing Application - System Design

**Version**: 1.0.0
**Target**: Spring Boot 3.x + JDK 17
**Purpose**: Parse MCA logs → Build FLD Header/Body → Call target system

---

## 🏗️ System Architecture

### High-Level Architecture
```
┌─────────────────┐
│   MCA Log File  │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────┐
│          MCA Log Parsing Application            │
│                                                  │
│  ┌──────────────┐    ┌─────────────────┐       │
│  │ Log Parser   │───▶│  FLD Builder    │       │
│  │  Component   │    │   (Header+Body) │       │
│  └──────────────┘    └────────┬────────┘       │
│                               │                 │
│                               ▼                 │
│                    ┌──────────────────┐         │
│                    │  Target System   │         │
│                    │  Client Service  │         │
│                    │  (RestTemplate)  │         │
│                    └────────┬─────────┘         │
│                             │                   │
│  ┌──────────────────────────┼─────────────┐    │
│  │  Testing Support         │             │    │
│  │  - Local Test Mode       │             │    │
│  │  - Main Method Test      │             │    │
│  │  - Mock Target System    │             │    │
│  └──────────────────────────┼─────────────┘    │
└───────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │  Target System   │
                    │  (External REST) │
                    └──────────────────┘
```

---

## 📦 Component Design

### 1. **MCA Log Parser**
**Package**: `com.mca.parser`

**Responsibilities**:
- Read and parse MCA log files
- Extract structured data from log entries
- Validate log format

**Key Classes**:
```java
@Component
public class McaLogParser {
    // Parse raw MCA log string to structured data
    public McaLogData parse(String rawLog);

    // Parse from file
    public List<McaLogData> parseFile(Path logFile);
}

public record McaLogData(
    String rawData,
    Map<String, String> fields,
    LocalDateTime timestamp
) {}
```

---

### 2. **FLD Message Builder**
**Package**: `com.mca.fld`

**Responsibilities**:
- Build FLD Header structure
- Build FLD Body structure
- Convert parsed data to FLD format

**Key Classes**:
```java
@Component
public class FldMessageBuilder {
    public FldMessage buildMessage(McaLogData logData);
}

// FLD Message Structure
public record FldMessage(
    FldHeader header,
    FldBody body,
    LocalDateTime createdAt
) {
    public String toFldString() {
        // Convert to fixed-length format
    }
}

// FLD Header
public record FldHeader(
    String messageType,      // 4 chars
    String transactionId,    // 20 chars
    String timestamp,        // 14 chars (yyyyMMddHHmmss)
    String systemCode        // 8 chars
) {
    private static final List<FieldSpec> SPECS = List.of(
        new FieldSpec("messageType", 4, FieldSpec.FieldType.STRING),
        new FieldSpec("transactionId", 20, FieldSpec.FieldType.STRING),
        new FieldSpec("timestamp", 14, FieldSpec.FieldType.STRING),
        new FieldSpec("systemCode", 8, FieldSpec.FieldType.STRING)
    );

    public static List<FieldSpec> specs() { return SPECS; }
}

// FLD Body
public record FldBody(
    String dataContent,      // Variable length
    int recordCount,         // Number of records
    Map<String, String> attributes
) {
    public String toFixedLength() {
        // Convert to FLD format without delimiters
    }
}
```

---

### 3. **Target System Client**
**Package**: `com.mca.client`

**Responsibilities**:
- Send FLD messages to target system
- Handle HTTP communication
- Process responses
- Implement retry logic

**Key Classes**:
```java
@Service
@RequiredArgsConstructor
public class TargetSystemClient {
    private final RestTemplate restTemplate;
    private final TargetSystemConfig config;

    public TargetSystemResponse send(FldMessage message) {
        try {
            String fldString = message.toFldString();

            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getEndpoint(),
                fldString,
                String.class
            );

            return TargetSystemResponse.success(
                response.getStatusCode().value(),
                response.getBody()
            );
        } catch (Exception e) {
            return TargetSystemResponse.failure(e.getMessage());
        }
    }
}

@ConfigurationProperties(prefix = "target.system")
@Data
public class TargetSystemConfig {
    private String endpoint;
    private int timeout;
    private int maxRetries;
}
```

---

### 4. **Response Model**
**Package**: `com.mca.model`

**Key Classes**:
```java
public record TargetSystemResponse(
    boolean success,
    int code,
    String message,
    LocalDate date,
    Instant timestamp
) {
    public static TargetSystemResponse success(int code, String message) {
        return new TargetSystemResponse(
            true,
            code,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }

    public static TargetSystemResponse failure(String message) {
        return new TargetSystemResponse(
            false,
            500,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }

    public static TargetSystemResponse of(boolean success, int code, String message) {
        return new TargetSystemResponse(
            success,
            code,
            message,
            LocalDate.now(),
            Instant.now()
        );
    }
}
```

---

### 5. **Testing Support**

#### Local Test Configuration
```java
@Configuration
@Profile("local")
public class LocalTestConfig {

    @Bean
    public RestTemplate mockRestTemplate() {
        return new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
                // Return mock response for local testing
                String mockResponse = "SUCCESS";
                return (ResponseEntity<T>) ResponseEntity.ok(mockResponse);
            }
        };
    }
}
```

#### Main Method Test Support
```java
@SpringBootApplication
public class McaParsingApplication {

    public static void main(String[] args) {
        if (args.length > 0 && "test".equals(args[0])) {
            // Test mode: run without server connection
            runTestMode(args);
        } else {
            // Normal mode: start Spring Boot application
            SpringApplication.run(McaParsingApplication.class, args);
        }
    }

    private static void runTestMode(String[] args) {
        System.out.println("=== MCA Parsing Test Mode ===");

        // Create test context
        ApplicationContext context = new SpringApplicationBuilder(McaParsingApplication.class)
            .profiles("local", "test")
            .run(args);

        // Get beans and execute test
        McaLogParser parser = context.getBean(McaLogParser.class);
        FldMessageBuilder builder = context.getBean(FldMessageBuilder.class);
        TargetSystemClient client = context.getBean(TargetSystemClient.class);

        // Sample test data
        String sampleLog = "MCA|20240101|120000|SAMPLE_DATA";

        try {
            // Test parsing
            McaLogData logData = parser.parse(sampleLog);
            System.out.println("✓ Parsing successful: " + logData);

            // Test FLD building
            FldMessage fldMessage = builder.buildMessage(logData);
            System.out.println("✓ FLD building successful: " + fldMessage.toFldString());

            // Test target system call
            TargetSystemResponse response = client.send(fldMessage);
            System.out.println("✓ Target system call successful: " + response);

            System.out.println("\n=== All Tests Passed ===");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```

---

## 📁 Project Structure

```
src/main/java/com/mca/
├── McaParsingApplication.java          # Main application with test mode
│
├── parser/                             # MCA Log Parsing
│   ├── McaLogParser.java              # Core parser
│   ├── model/
│   │   ├── McaLogData.java            # Parsed log data
│   │   └── FieldSpec.java             # Field specification
│   └── validator/
│       └── LogValidator.java          # Validation logic
│
├── fld/                                # FLD Message Building
│   ├── FldMessageBuilder.java         # Message builder
│   ├── model/
│   │   ├── FldMessage.java            # Complete message
│   │   ├── FldHeader.java             # Header structure
│   │   └── FldBody.java               # Body structure
│   └── formatter/
│       └── FixedLengthFormatter.java  # Fixed-length conversion
│
├── client/                             # Target System Integration
│   ├── TargetSystemClient.java        # REST client
│   ├── config/
│   │   ├── TargetSystemConfig.java    # Configuration
│   │   └── RestTemplateConfig.java    # RestTemplate setup
│   └── retry/
│       └── RetryPolicy.java           # Retry logic
│
├── model/                              # Shared Models
│   └── TargetSystemResponse.java      # Response model
│
├── config/                             # Configuration
│   ├── LocalTestConfig.java           # Local test configuration
│   └── ApplicationConfig.java         # General configuration
│
└── api/                                # REST API (optional)
    └── ParsingController.java         # HTTP endpoints

src/main/resources/
├── application.yml                     # Default configuration
├── application-local.yml               # Local profile
├── application-dev.yml                 # Development profile
└── application-prod.yml                # Production profile

src/test/java/com/mca/
├── parser/
│   └── McaLogParserTest.java
├── fld/
│   └── FldMessageBuilderTest.java
├── client/
│   └── TargetSystemClientTest.java
└── integration/
    └── EndToEndTest.java
```

---

## ⚙️ Configuration Design

### application.yml
```yaml
spring:
  application:
    name: mca-parsing-application
  profiles:
    active: local

# Target System Configuration
target:
  system:
    endpoint: ${TARGET_ENDPOINT:http://localhost:8080/api/receive}
    timeout: 5000
    max-retries: 3

# Logging
logging:
  level:
    com.mca: DEBUG
    org.springframework: INFO
```

### application-local.yml
```yaml
target:
  system:
    endpoint: http://localhost:8080/mock/api
    timeout: 1000
    max-retries: 0

logging:
  level:
    com.mca: TRACE
```

### application-prod.yml
```yaml
target:
  system:
    endpoint: ${TARGET_ENDPOINT}
    timeout: 10000
    max-retries: 5

logging:
  level:
    com.mca: INFO
```

---

## 🧪 Testing Strategy

### 1. **Unit Tests**
- Parser logic validation
- FLD formatting verification
- Response model creation

### 2. **Integration Tests**
- End-to-end flow testing
- Mock target system responses
- Error handling scenarios

### 3. **Local Testing**
```bash
# Run with local profile (mock target system)
./gradlew bootRun --args='--spring.profiles.active=local'

# Run test mode via main method
./gradlew bootRun --args='test'
```

### 4. **Main Method Testing**
```bash
# Direct test execution without server connection
java -jar app.jar test

# With custom test data
java -jar app.jar test --data="sample-mca.log"
```

---

## 🚀 Build & Dependency Configuration

### build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.mca'
version = '1.0.0'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Configuration
    implementation 'org.springframework.boot:spring-boot-configuration-processor'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.assertj:assertj-core'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 📊 Key Design Decisions

### 1. **Java 17 Records**
- Use records for immutable data structures (FldHeader, FldBody, Response)
- Cleaner syntax, built-in equals/hashCode/toString
- Thread-safe by default

### 2. **RestTemplate**
- Simple, synchronous HTTP client
- Easy to test and mock
- Sufficient for target system integration

### 3. **Configuration Profiles**
- `local`: Mock target system for local development
- `dev`: Development environment with real endpoints
- `prod`: Production configuration with full retry/timeout

### 4. **Main Method Test Support**
- No server connection required for testing
- Quick validation of parsing logic
- Useful for CI/CD pipeline verification

### 5. **Fixed-Length FLD Format**
- Based on FieldSpec pattern from reference files
- Type-safe conversion
- Easy to maintain and extend

---

## 🔄 Processing Flow

```
1. Read MCA Log
   ↓
2. Parse Log → McaLogData
   ↓
3. Build FLD Message
   ├─ Build FLD Header (fixed-length fields)
   └─ Build FLD Body (data content)
   ↓
4. Convert to FLD String (remove delimiters)
   ↓
5. Send to Target System (RestTemplate)
   ↓
6. Receive Response
   ↓
7. Create TargetSystemResponse
   ├─ success: true/false
   ├─ code: HTTP status
   ├─ message: response body
   ├─ date: LocalDate.now()
   └─ timestamp: Instant.now()
```

---

## 📝 Sample Usage

### Programmatic Usage
```java
@Service
@RequiredArgsConstructor
public class McaProcessingService {

    private final McaLogParser parser;
    private final FldMessageBuilder builder;
    private final TargetSystemClient client;

    public TargetSystemResponse process(String mcaLog) {
        // Parse MCA log
        McaLogData logData = parser.parse(mcaLog);

        // Build FLD message
        FldMessage fldMessage = builder.buildMessage(logData);

        // Send to target system
        return client.send(fldMessage);
    }
}
```

### REST API Usage
```java
@RestController
@RequestMapping("/api/v1/mca")
@RequiredArgsConstructor
public class McaController {

    private final McaProcessingService service;

    @PostMapping("/process")
    public ResponseEntity<TargetSystemResponse> process(@RequestBody String mcaLog) {
        TargetSystemResponse response = service.process(mcaLog);
        return ResponseEntity.ok(response);
    }
}
```

---

## ✅ Design Validation Checklist

- [x] JDK 17 compatible (records, modern syntax)
- [x] Spring Boot 3.x architecture
- [x] MCA log parsing capability
- [x] FLD Header/Body structure
- [x] Target system integration (RestTemplate)
- [x] Response model (success, code, message, date, timestamp)
- [x] Local testing support
- [x] Main method test capability
- [x] Server-less test mode
- [x] Clean architecture (separation of concerns)
- [x] Modern Java patterns (records, sealed classes potential)
- [x] Configuration profile support
- [x] Comprehensive testing strategy

---

## 🎯 Next Steps

1. **Implementation Phase**
   - Create base project structure
   - Implement core parsing logic
   - Build FLD message components
   - Implement target system client

2. **Testing Phase**
   - Write unit tests
   - Create integration tests
   - Validate local test mode
   - Test main method execution

3. **Documentation Phase**
   - API documentation
   - Configuration guide
   - Deployment instructions

---

**Design Status**: ✅ Ready for Implementation
**Complexity**: Moderate
**Estimated Implementation Time**: 2-3 days
