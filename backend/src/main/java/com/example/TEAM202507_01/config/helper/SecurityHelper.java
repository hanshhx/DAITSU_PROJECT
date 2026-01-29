package com.example.TEAM202507_01.config.helper; // 1. 이 클래스가 위치한 폴더 경로입니다.

// 2. [Imports] 스프링 시큐리티의 핵심 인증 관련 클래스들을 가져옵니다.
import com.example.TEAM202507_01.config.jwt.JwtFilter;
import org.springframework.security.core.Authentication; // 인증된 사용자의 정보(신분증)를 담는 객체
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder; // 현재 스레드(요청)의 보안 정보를 저장하는 금고
import org.springframework.security.core.userdetails.UserDetails; // 스프링 시큐리티가 사용하는 표준 유저 정보 인터페이스

import java.util.Optional; // 값이 있을 수도 있고 없을 수도 있을 때, Null 에러를 방지하는 포장 박스

// 3. [Class Definition] 보안 관련 도움을 주는 유틸리티 클래스입니다.
public class SecurityHelper {

    // 4. [Method] 현재 로그인한 사용자의 아이디를 꺼내오는 메서드입니다.
    // static으로 선언해서 'new SecurityHelper()' 없이 바로 갖다 쓸 수 있게 합니다.
    // 반환타입 Optional<String>: 아이디가 있으면 문자열을 주고, 비로그인 상태면 빈 상자를 줍니다.
    public static Optional<String> getLoggedId() {

        // 5. [Access Context] 금고(SecurityContextHolder)에서 현재 인증 정보(Authentication)를 꺼냅니다.
        // 이 정보는 앞단에 있는 'JwtFilter'가 요청이 들어올 때 미리 넣어둔 것입니다.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 6. [Check Null] 만약 꺼낸 인증 정보가 없다면? (로그인 안 한 상태거나, 인증 과정 오류)
        if (authentication == null) {
            // 빈 상자(Optional.empty)를 반환하고 끝냅니다. "누군지 모르겠어요"
            return Optional.empty();
        }

        // 7. 아이디를 담을 변수를 준비합니다. (일단 null로 시작)
        String loggedId = null;

        // 8. [Check Type 1] 인증 정보의 주체(Principal)가 'UserDetails' 타입인지 확인합니다.
        // UserDetails는 스프링 시큐리티의 기본 유저 객체입니다. (가장 일반적인 경우)
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            // 맞다면, getUsername() 메서드를 통해 아이디를 꺼냅니다.
            loggedId = springSecurityUser.getUsername();

            // 9. [Check Type 2] 혹은 주체(Principal)가 그냥 'String' 문자열인지 확인합니다.
            // 가끔 JWT 설정에 따라 객체가 아니라 아이디 문자열만 덜렁 저장해놓는 경우가 있어서 확인합니다.
        } else if (authentication.getPrincipal() instanceof String) {
            // 맞다면, 강제 형변환((String))을 해서 문자열 자체를 아이디로 씁니다.
            loggedId = (String) authentication.getPrincipal();
        }

        // 10. [Return] 찾은 아이디(loggedId)를 Optional 상자에 담아서 반환합니다.
        // 만약 위 과정을 거쳤는데도 loggedId가 null이면, 빈 상자가 반환되어 NullPointerException을 막아줍니다.
        return Optional.ofNullable(loggedId);
    }
}


//        요청 도착: 사용자가 글 수정 요청을 보냅니다. 이때 헤더에는 **JWT 토큰(출입증)**이 들어있습니다.
//
//        검문소 통과 (JwtFilter): (이 코드 이전 단계) 필터가 토큰을 검사하고, 유효하다면 SecurityContext라는 금고에 "지금 접속한 사람은 'user123'이야"라고 신분증(Authentication)을 넣어둡니다.
//
//        서비스 로직 실행: 이제 "글 수정 서비스"가 작동합니다. 그런데 이 서비스는 누가 글을 수정하려는지 알아야 합니다.
//
//        헬퍼 호출 (getLoggedId):
//
//        서비스는 SecurityHelper.getLoggedId()를 호출합니다.
//
//        이 헬퍼는 SecurityContext 금고를 엽니다.
//
//        그 안에 있는 신분증(Authentication)을 꺼내서 이름을 확인합니다.
//
//        신원 확인: "아, 지금 접속자는 'user123'이구나!"라고 파악해서 서비스에게 돌려줍니다.