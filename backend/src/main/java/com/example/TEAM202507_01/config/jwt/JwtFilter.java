package com.example.TEAM202507_01.config.jwt; // 1. 파일의 위치(패키지)

// --- [Imports] 필요한 도구들을 가져옵니다. ---
import com.example.TEAM202507_01.config.exception.ErrorDetails; // 에러 났을 때 보낼 메시지 포맷
import com.example.TEAM202507_01.config.security.CustomUserDetailsService; // DB에서 유저 정보 가져오는 서비스
import com.fasterxml.jackson.databind.ObjectMapper; // 자바 객체를 JSON 문자열로 바꿔주는 도구
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 날짜(LocalDateTime)를 JSON으로 잘 바꾸기 위한 모듈
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer; // 날짜 포맷 설정기
import jakarta.servlet.FilterChain; // 다음 필터로 넘겨주는 연결고리
import jakarta.servlet.ServletException; // 서블릿 관련 에러
import jakarta.servlet.ServletRequest; // 일반적인 요청 객체
import jakarta.servlet.ServletResponse; // 일반적인 응답 객체
import jakarta.servlet.http.Cookie; // 쿠키 다루는 도구
import jakarta.servlet.http.HttpServletRequest; // HTTP 전용 요청 객체 (쿠키, 헤더 기능 포함)
import jakarta.servlet.http.HttpServletResponse; // HTTP 전용 응답 객체
import lombok.RequiredArgsConstructor; // final 변수 자동 생성자 (의존성 주입)
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 인증 완료된 유저의 신분증 객체
import org.springframework.security.core.Authentication; // '인증' 그 자체를 나타내는 인터페이스
import org.springframework.security.core.context.SecurityContextHolder; // 인증 정보를 담아두는 '전역 금고'
import org.springframework.security.core.userdetails.UserDetails; // 유저 상세 정보 인터페이스
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component; // "스프링아, 이 클래스 네가 관리해줘(Bean 등록)"
import org.springframework.util.StringUtils; // 문자열 검사 유틸리티 (null 체크 등)
import org.springframework.web.filter.GenericFilterBean; // 필터 기능을 쉽게 만들기 위한 부모 클래스

import java.io.IOException; // 입출력 에러 처리
import java.time.LocalDateTime; // 날짜 시간 처리
import java.time.format.DateTimeFormatter; // 날짜 포맷팅 도구

@Component // 1. 이 필터를 스프링 컨테이너에 빈으로 등록합니다.
@RequiredArgsConstructor // 2. final이 붙은 필드들(tokenProvider 등)을 채워주는 생성자를 자동으로 만듭니다.
public class JwtFilter extends GenericFilterBean { // GenericFilterBean을 상속받아 필터 역할을 수행합니다.

    // 3. 토큰을 만들고 검증하는 전문가 (의존성 주입)
    private final TokenProvider tokenProvider;

    // 4. DB에서 유저 정보를 가져오는 심부름꾼 (의존성 주입)
    private final CustomUserDetailsService customUserDetailsService;

    // 5. 헤더에서 토큰 찾을 때 쓸 이름표 ("Authorization")
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * [핵심 메서드] 필터링 로직이 수행되는 곳입니다.
     * 모든 요청은 이 메서드를 거쳐가야 합니다.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 6. 들어온 요청(ServletRequest)을 HTTP 기능(쿠키, 헤더 등)을 쓰기 위해 형변환합니다.
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        // 7. [Step 1] 요청에서 토큰을 찾아냅니다. (아래 resolveToken 메서드가 일을 합니다.)
        // 쿠키를 먼저 뒤지고, 없으면 헤더를 뒤져서 문자열 토큰을 가져옵니다.
        String token = resolveToken(httpServletRequest);

        try {
            // 8. [Step 2] 토큰 검사 시작
            // 토큰이 존재하고(hasText), 위조되지 않았으며 유효기간이 남았는지(isValidToken) 확인합니다.
            if (StringUtils.hasText(token) && tokenProvider.isValidToken(token)) {

                // 9. [Step 3] 토큰이 유효하다면, 토큰 안에 숨겨진 '로그인 ID'를 꺼냅니다.
                String userId = tokenProvider.getLoginId(token);

                // 10. [Step 4] 꺼낸 ID를 가지고 DB에서 진짜 유저 정보(비번, 권한 등)를 싹 긁어옵니다.
                // 여기서 유저가 없으면 에러가 날 수 있습니다.
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

                // 11. [Step 5] "인증 성공!" 도장이 찍힌 신분증(Authentication 객체)을 만듭니다.
                // 인자: (유저정보, 패스워드-보통 null처리, 권한목록)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 12. [Step 6] 만든 신분증을 스프링 시큐리티의 '보안 금고(SecurityContext)'에 넣어둡니다.
                // 이제부터 이 요청이 끝날 때까지 서버는 "이 사람은 인증된 사람이야"라고 알게 됩니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 13. [Pass] 검사가 끝났으니(혹은 토큰이 없어서 그냥 넘어가더라도), 다음 필터나 컨트롤러로 요청을 넘겨줍니다.
            // "지나가세요~"
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            // 14. [Exception Handling] 토큰 검사 중 에러가 발생했다면? (위조, 만료, DB오류 등)

            // 응답 객체를 HTTP용으로 형변환합니다.
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

            // 15. 상태 코드를 401 (Unauthorized - 인증 안됨)로 설정합니다.
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // 16. 응답 형식을 JSON으로 설정합니다. (프론트엔드가 알아먹기 쉽게)
            httpResponse.setContentType("application/json");

            // 17. 에러 내용을 담을 객체를 만듭니다. (시간, 에러메시지, 문제된 토큰)
            ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), e.getMessage(), token);

            // 18. [JSON Conversion] 자바 객체를 JSON 문자열로 바꾸기 위한 준비 과정입니다.
            // 날짜(LocalDateTime)가 JSON으로 예쁘게 바뀌도록 모듈을 설정합니다.
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")));

            // 19. 실제로 객체를 JSON 문자열로 변환합니다.
            String json = new ObjectMapper().registerModule(javaTimeModule).writeValueAsString(errorDetails);

            // 20. 응답 본문(Body)에 JSON 에러 메시지를 써서 보냅니다.
            httpResponse.getWriter().write(json);
        }
    }

    /**
     * [보조 메서드] 요청에서 토큰 문자열만 쏙 빼오는 함수
     */
    private String resolveToken(HttpServletRequest request) {
        String token = null;

        // 1. [Priority 1] 쿠키 주머니를 먼저 뒤집니다. (우선순위 높음)
        if (request.getCookies() != null) {
            // 쿠키가 여러 개일 수 있으니 하나씩 확인합니다.
            for (Cookie c : request.getCookies()) {
                // ★ 중요: 쿠키 이름이 "token"인 것을 찾습니다.
                if ("token".equals(c.getName())) {
                    token = c.getValue(); // 찾았으면 값을 꺼냅니다.
                    break; // 찾았으면 반복문 종료 (더 볼 필요 없음)
                }
            }
        }

        // 2. [Priority 2] 쿠키에 토큰이 없었다면? 헤더를 뒤집니다.
        if (!StringUtils.hasText(token)) {
            // 헤더에서 "Authorization" 값을 꺼냅니다.
            String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

            // 값이 있고, 그 값이 "Bearer "로 시작하는지 확인합니다. (표준 규격)
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                // 앞의 "Bearer " (7글자)를 잘라내고 순수 토큰 값만 가집니다.
                token = bearerToken.substring(7);
            }
        }

        // 3. 찾은 토큰을 반환합니다. (못 찾았으면 null 반환)
        return token;
    }
}

//
//검문 시작 (doFilter):
//
//사용자의 요청이 컨트롤러(마이페이지)에 도착하기 전에, 이 필터가 낚아챕니다.
//
//출입증 수색 (resolveToken):
//
//보안요원은 사용자의 주머니(쿠키)를 먼저 뒤져서 token이 있는지 봅니다.
//
//만약 주머니에 없으면, 목에 건 목걸이(헤더)에 Authorization 태그가 있는지 확인합니다.
//
//위조 검사 (tokenProvider.isValidToken):
//
//        토큰을 찾았다면, 이게 위조된 건지 유효기간이 지났는지 기계를 돌려 확인합니다.
//
//        신원 조회 (loadUserByUsername):
//
//        유효한 토큰이라면, 토큰 안에 적힌 아이디("user1")를 꺼냅니다.
//
//그리고 DB 담당자(UserDetailsService)에게 연락해서 "user1 님의 상세 정보(권한 등) 좀 보내줘"라고 합니다.
//
//출입 허가 (SecurityContextHolder):
//
//확인된 정보를 바탕으로 **"임시 출입증(Authentication)"**을 만들어서 **보안 금고(Context)**에 넣어둡니다.
//
//이제 스프링 시큐리티는 "아, 얘는 인증된 사용자구나"라고 인지하고 통과시켜 줍니다(chain.doFilter).
//
//퇴짜 (Exception):
//
//만약 토큰이 짝퉁이거나 만료되었다면? 바로 "401 Unauthorized" 에러와 함께 JSON 메시지를 던져서 쫓아냅니다.