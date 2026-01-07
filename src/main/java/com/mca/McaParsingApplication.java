package com.mca;

import com.mca.client.TargetSystemClient;
import com.mca.fld.FldMessageBuilder;
import com.mca.fld.model.FldMessage;
import com.mca.model.TargetSystemResponse;
import com.mca.parser.McaLogParser;
import com.mca.parser.model.McaLogData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

/**
 * MCA 로그 파싱 애플리케이션
 */
@Slf4j
@SpringBootApplication
public class McaParsingApplication {

    public static void main(String[] args) {
        // 첫 번째 인자가 'test'인 경우 테스트 모드로 실행
        if (args.length > 0 && "test".equals(args[0])) {
            runTestMode(args);
        } else {
            // 일반 모드: Spring Boot 애플리케이션 시작
            SpringApplication.run(McaParsingApplication.class, args);
        }
    }

    /**
     * 테스트 모드 실행 (서버 연결 없이 로컬 테스트)
     */
    private static void runTestMode(String[] args) {
        System.out.println("==============================================");
        System.out.println("  MCA 파싱 애플리케이션 - 테스트 모드");
        System.out.println("==============================================");
        System.out.println();

        try {
            // Spring 컨텍스트 생성 (local 프로파일 사용)
            ApplicationContext context = new SpringApplicationBuilder(McaParsingApplication.class)
                .profiles("local", "test")
                .run(args);

            // 필요한 빈 가져오기
            McaLogParser parser = context.getBean(McaLogParser.class);
            FldMessageBuilder builder = context.getBean(FldMessageBuilder.class);
            TargetSystemClient client = context.getBean(TargetSystemClient.class);

            // 샘플 MCA 로그 데이터
            String sampleLog = "MCA0|20240101|120000|SAMPLE_DATA_FOR_TESTING";

            System.out.println("1. MCA 로그 파싱 테스트");
            System.out.println("   원본 로그: " + sampleLog);
            System.out.println();

            // 1단계: MCA 로그 파싱
            McaLogData logData = parser.parse(sampleLog);
            System.out.println("   ✓ 파싱 성공");
            System.out.println("   - 메시지 타입: " + logData.getField("messageType"));
            System.out.println("   - 날짜: " + logData.getField("date"));
            System.out.println("   - 시간: " + logData.getField("time"));
            System.out.println("   - 바디: " + logData.getField("body"));
            System.out.println();

            // 2단계: FLD 메시지 빌드
            System.out.println("2. FLD 메시지 빌드 테스트");
            FldMessage fldMessage = builder.buildMessage(logData);
            System.out.println("   ✓ FLD 메시지 빌드 성공");
            System.out.println("   - 메시지 길이: " + fldMessage.totalLength() + " bytes");
            System.out.println("   - FLD 문자열: " + fldMessage.toFldString());
            System.out.println();

            // 3단계: 타겟 시스템 호출
            System.out.println("3. 타겟 시스템 호출 테스트");
            TargetSystemResponse response = client.send(fldMessage);
            System.out.println("   ✓ 타겟 시스템 호출 성공");
            System.out.println("   - Success: " + response.success());
            System.out.println("   - Code: " + response.code());
            System.out.println("   - Message: " + response.message());
            System.out.println("   - Date: " + response.date());
            System.out.println("   - Timestamp: " + response.timestamp());
            System.out.println();

            System.out.println("==============================================");
            System.out.println("  ✅ 모든 테스트 통과");
            System.out.println("==============================================");

            System.exit(0);

        } catch (Exception e) {
            System.err.println();
            System.err.println("==============================================");
            System.err.println("  ❌ 테스트 실패");
            System.err.println("==============================================");
            System.err.println("오류: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
