package com.example.TEAM202507_01.config.exception;


import com.example.TEAM202507_01.config.property.ErrorMessagePropertySource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//의미: 이 클래스를 스프링이 관리하는 부품(Bean)으로 등록함.
@RequiredArgsConstructor
//의미: final이 붙은 필드들을 파라미터로 받는 생성자를 자동으로 생성함.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    //implements AuthenticationEntryPoint: "나는 인증 실패(401) 상황을 전담해서 처리하는 클래스다"라고 규격에 맞게 선언한 것임

    private final ErrorMessagePropertySource errorMessagePropertySource;
    //private final: 값이 변하지 않도록 고정함.
    //errorMessagePropertySource: 외부 설정 파일에서 "유효하지 않은 토큰입니다" 같은 메시지를 읽어오는 도구

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        //commence: 단어 뜻 그대로 '시작하다'는 의미임. 인증 실패가 감지되면 이 메서드가 실행되며 응답 프로세스를 시작함
        //HttpServletRequest: 클라이언트가 보낸 요청 정보임.
        //AuthenticationException: 어떤 이유로 인증이 실패했는지 정보를 담고 있음.

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessagePropertySource.getInvalidToken());
        //response.sendError: 클라이언트에게 에러 상태와 메시지를 보냄.
        //HttpServletResponse.SC_UNAUTHORIZED: 숫자 401을 의미함. "너 인증 안 됐어(Unauthorized)"라는 표준 응답 코드임
        //errorMessagePropertySource.getInvalidToken(): 설정 파일에서 "토큰이 잘못되었습니다"라는 메시지를 가져와서 응답에 실어 보냄.
    }
}
