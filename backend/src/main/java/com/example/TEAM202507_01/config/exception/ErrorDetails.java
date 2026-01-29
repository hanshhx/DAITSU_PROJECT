package com.example.TEAM202507_01.config.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
// 클래스의 모든 필드(timestamp, message, details)를 매개변수로 받는 생성자를 자동으로 생성함

@Getter
//모든 필드에 대한 Getter 메서드를 자동으로 생성함. Spring이 이 객체를 JSON으로 변환할 때 접근할 수 있게 해줌.
public class ErrorDetails {
    private LocalDateTime timestamp;
    //에러가 발생한 정확한 시간을 기록함.
    private String message;
    //에러에 대한 요약 메시지(예: "사용자를 찾을 수 없음")를 담음.
    private String details;
    //에러의 상세 내용이나 요청한 URL 경로 등을 기록하여 디버깅을 도움.
}
