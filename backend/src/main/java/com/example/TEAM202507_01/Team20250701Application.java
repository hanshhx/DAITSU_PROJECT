package com.example.TEAM202507_01;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.ibatis.annotations.Mapper;

@MapperScan(
        basePackages = "com.example.TEAM202507_01",
        annotationClass = Mapper.class
)
@SpringBootApplication
public class Team20250701Application {

    public static void main(String[] args) {
        // 1. 서버 시작 알림
        System.out.println("\n\n🔥 [DEBUG START] 서버 실행 명령을 받았습니다. 부팅을 시작합니다... 🔥\n");

        try {
            // 2. 스프링 부트 실행 시도
            SpringApplication.run(Team20250701Application.class, args);

            // 3. 성공 시 메시지
            System.out.println("\n✅ [DEBUG SUCCESS] 서버가 정상적으로 켜졌습니다! (8080 포트 대기 중)\n");

        } catch (Throwable e) {
            // 4. ★ 실행 중 죽으면 여기서 에러를 잡아서 출력합니다 ★
            System.err.println("\n\n❌ ❌ ❌ [CRITICAL ERROR] 서버 실행 중 치명적인 오류로 사망했습니다! ❌ ❌ ❌");
            System.err.println("▼ ▼ ▼ [에러 원인 분석] ▼ ▼ ▼\n");

            // 에러 내용 전체 출력 (이걸 보여주셔야 합니다)
            e.printStackTrace();

            System.err.println("\n▲ ▲ ▲ [에러 로그 끝] ▲ ▲ ▲\n");
        }
    }
}