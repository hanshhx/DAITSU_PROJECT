package com.example.TEAM202507_01.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
//로그(기록)를 남기기 위한 도구임. 개발자가 서버 콘솔에 System.out.println 대신 전문적인 로그를 남길 때 사용함

@RestControllerAdvice
// 프로젝트 전체(Global)의 컨트롤러(Controller)에서 발생하는 예외를 감시하고 조언(Advice)하겠다는 뜻임
// 에러가 났을 때 클라이언트(프론트엔드)에게 예쁜 JSON 형태로 응답을 돌려주기 위해 사용함.
public class GlobalExceptionHandler {

    // 모든 에러를 다 잡아서 콘솔에 출력
    @ExceptionHandler(Exception.class)
    //@ExceptionHandler: "이 메서드는 예외를 처리하는 전용 메서드다"라고 지정함
    //(Exception.class): 자바의 모든 예외의 조상인 Exception을 지정했음. 즉, 세상의 모든 에러를 다 이 메서드가 잡겠다는 의미

    public ResponseEntity<Map<String, String>> handleAllException(Exception e) {
        //ResponseEntity: 단순한 데이터뿐만 아니라 HTTP 상태 코드(예: 200, 404, 500)를 같이 담아서 보낼 수 있는 봉투 역할을 함.
        //<Map<String, String>>: 봉투 안에 담을 내용물의 형식이 "글자로 된 이름: 글자로 된 내용"인 Map 구조라는 뜻
        //(Exception e): 발생한 에러의 정보를 e라는 이름으로 받아서 메서드 안으로 가져옴.

        log.error("🔥🔥🔥 [서버 에러 발생] 🔥🔥🔥", e); // 콘솔에 빨간색 에러 로그 출력
        //log.error: 서버 관리자가 보는 화면(콘솔)에 에러를 기록함. 불 이모지를 써서 에러가 났음을 한눈에 알게 함.

        e.printStackTrace(); // 상세 에러 내용 출력
        //에러가 코드 몇 번째 줄에서, 어떤 경로를 타고 발생했는지 상세히 출력함

        Map<String, String> response = new HashMap<>();
        //프론트엔드에 보내줄 데이터를 담을 빈 HashMap 바구니를 만듦.

        response.put("error", "서버 내부 오류 발생");
        response.put("message", e.getMessage()); // 에러 메시지를 프론트엔드로 보냄
        //error: 에러의 제목
        //message: 실제 에러가 왜 났는지 e.getMessage()를 통해 상세 내용을 가져와서 담음.

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        //status(HttpStatus.INTERNAL_SERVER_ERROR): HTTP 응답 코드를 500으로 설정함. 서버에 문제가 생겼음을 공식적으로 알림
        //.body(response): 위에서 만든 데이터 바구니를 내용물로 채워서 보냄

    }
}