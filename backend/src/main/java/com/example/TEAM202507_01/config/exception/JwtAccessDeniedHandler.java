package com.example.TEAM202507_01.config.exception;

import com.example.TEAM202507_01.config.property.ErrorMessagePropertySource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//의미: 이 클래스를 스프링이 관리하는 '부품(Bean)'으로 등록
@RequiredArgsConstructor
//의미: final이 붙은 필드(변수)를 인자로 받는 생성자를 자동으로 만들어줌.

public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    //implements AccessDeniedHandler: "나는 이제부터 권한 거부 상황을 전문적으로 처리하는 핸들러다"라고 선언하는 것임.
    // 스프링 시큐리티의 규격을 따르겠다는 의미임.

    private final ErrorMessagePropertySource errorMessagePropertySource;
    //private final: 한 번 정해지면 바뀌지 않는 상수로 선언함. 객체의 불변성을 보장함.
    //ErrorMessagePropertySource: 에러 메시지가 적힌 문서(Property)에서 실제 메시지를 가져오는 도구

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        //// 인터페이스에 정의된 handle 메서드를 내 입맛에 맞게 재정의함.
        //handle 메서드: 권한 거부 에러가 발생하는 순간 스프링 시큐리티가 이 메서드를 자동으로 호출함.
        //인자들: 요청 정보(request), 응답 보낼 통로(response), 발생한 에러 정보(accessDeniedException)를 전달받음.

        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessagePropertySource.getForbidden());
        //response.sendError: 클라이언트(브라우저)에게 "너 에러 났어"라고 공식적으로 알림.
        //HttpServletResponse.SC_FORBIDDEN: 숫자 403을 의미함. "금지됨(Forbidden)"이라는 표준 HTTP 상태 코드임
        //errorMessagePropertySource.getForbidden(): "접근 권한이 없습니다" 같은 미리 설정된 한글/영어 메시지를 가져와서 응답에 담음.

    }
}
