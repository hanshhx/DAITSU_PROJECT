package com.example.TEAM202507_01.config; // 1. 이 설정 파일이 위치한 패키지 주소

// 2. [Imports] 시큐리티 설정, 필터, 로거, CORS 등 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.config.jwt.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // ★ 추가된 import
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration // 3. "스프링아, 이건 설정 파일이야. 서버 켤 때 제일 먼저 읽어줘."
@EnableWebSecurity // 4. "이제부터 스프링 시큐리티가 웹 보안을 책임진다!" (보안 기능 활성화)
@RequiredArgsConstructor // 5. final 변수(jwtFilter)를 채워주는 생성자 자동 생성
public class SecurityConfig {

    // 6. [Logger] 보안 관련 로그(누가 차단됐는지 등)를 찍기 위한 기록원 생성
    private final Logger log = LoggerFactory.getLogger(getClass());

    // 7. [Filter] 우리가 만든 JWT 검문소(필터)를 가져옵니다. (시큐리티 체인에 끼워 넣기 위해)
    private final JwtFilter jwtFilter;

    // ★★★ [핵심 추가] 정적 리소스(이미지, CSS, JS) 프리패스 설정 ★★★
    // 이 설정이 없으면 이미지 하나 불러올 때도 토큰 검사를 하려고 해서 이미지가 깨지거나 403 에러가 납니다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 8. "이미지, CSS, 자바스크립트 파일은 보안 필터를 아예 거치지 말고(ignoring) 그냥 줘라!"
        // 성능 향상에도 도움이 됩니다.
        return (web) -> web.ignoring()
                .requestMatchers("/images/**", "/css/**", "/js/**");
    }

    // 9. [Main Security Logic] 실제 보안 규칙을 정하는 핵심 메서드입니다.
    // 어떤 요청을 허용하고, 어떤 필터를 쓰고, 에러는 어떻게 처리할지 여기서 다 정합니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 10. [CSRF 비활성화] CSRF는 위조 요청 방지 기능인데,
                // 우리는 세션이 아니라 '토큰'을 쓰기 때문에 굳이 필요 없어서 끕니다. (켜면 복잡해짐)
                .csrf(AbstractHttpConfigurer::disable)

                // 🔥 [추가 1] [CORS 설정] "다른 도메인(프론트엔드)에서 오는 요청도 받아줘"
                // 아래에 만든 corsConfigurationSource() 설정을 적용합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 11. [세션 비활성화] "서버에 로그인 정보를 저장(Session)하지 마."
                // JWT는 매번 토큰을 검사하므로 서버가 상태를 기억할 필요가 없습니다(STATELESS).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 12. https://blog.naver.com/lawfirmhankyul/223410161191?viewType=pc "이 주소는 누구에게 열어줄까?"
                .authorizeHttpRequests(auth -> auth
                        // 13. 커뮤니티, 채용정보 관련 API는 로그인 안 해도 누구나 볼 수 있게 허용 (permitAll)
                        .requestMatchers("/api/v1/community/**", "/api/v1/job/**").permitAll()
                        // 14. 관리자 페이지 방문자 수 기록 API도 누구나 호출 가능하게 허용
                        .requestMatchers("/api/v1/admin/visit").permitAll()
                        // 15. [중요] 그 외의 모든 요청도 일단은 다 허용합니다.
                        // (개발 편의를 위해 이렇게 둔 것 같네요. 나중에 .authenticated()로 바꾸면 로그인한 사람만 가능해집니다.)
                        .anyRequest().permitAll()
                )

                // 16. [필터 순서 지정] ★★★ 제일 중요한 부분!
                // 기본 로그인 필터(UsernamePasswordAuthenticationFilter)가 돌기 "전에(Before)"
                // 우리가 만든 jwtFilter가 먼저 실행되게 끼워 넣습니다.
                // 즉, 아이디/비번 검사하기 전에 토큰 검사부터 먼저 합니다.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 17. [에러 핸들링] 인증/인가 실패 시 어떻게 대응할지 설정
                .exceptionHandling(error -> error
                        // 18. [인증 실패(401)] 로그인 안 했는데 들어오려 하거나 토큰이 이상할 때
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 경고 로그를 남깁니다.
                            log.warn("🛑 [인증 실패 - 401] : {} || 원인: {}", request.getRequestURI(), authException.getMessage());
                            // 사용자에게 401 Unauthorized 에러를 보냅니다.
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        // 19. [인가 실패(403)] 로그인은 했는데 관리자만 가는 곳에 일반 유저가 가려 할 때
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 경고 로그를 남깁니다.
                            log.warn("🚫 [권한 거부 - 403] : {} || 원인: {}", request.getRequestURI(), accessDeniedException.getMessage());
                            // 사용자에게 403 Forbidden 에러를 보냅니다.
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                );

        // 20. 설정 완료된 보안 체인을 반환합니다.
        return http.build();
    }

    // 21. [Server Address] application.properties에서 서버 주소를 가져옵니다. (없으면 localhost)
    @Value("${server.address:localhost}")
    String serveraddress;

    // 🔥 [추가 2] [CORS 상세 설정] 프론트엔드 접속 허가증 발급처
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 22. [허용할 출처] 프론트엔드 서버 주소(localhost:3000)를 허용 리스트에 넣습니다.
        // IP 주소로 접속하는 경우도 대비해 serveraddress 변수도 활용합니다.
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://" + serveraddress + ":3000"));

        // 23. [허용할 메서드] GET(조회), POST(생성), PUT(수정), DELETE(삭제) 등 모든 방식 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 24. [허용할 헤더] 어떤 헤더를 보내도 다 받아줌
        config.setAllowedHeaders(List.of("*"));

        // 25. [쿠키 허용] "자격 증명(쿠키, 인증헤더)을 포함해서 요청해도 돼?" -> OK(true)
        // 이게 true여야 프론트엔드가 로그인 쿠키를 주고받을 수 있습니다.
        config.setAllowCredentials(true);

        // 26. 위 설정을 모든 경로("/**")에 적용하겠다고 등록합니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 27. [AuthenticationManager] 로그인 처리를 담당하는 매니저를 빈으로 등록합니다.
    // 나중에 AuthService 같은 곳에서 "로그인 시도해줘"라고 시킬 때 필요합니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

//
//이미지 요청 (프리패스):
//
//사용자가 화면에 띄울 로고 이미지(/images/logo.png)를 요청합니다.
//
//        **webSecurityCustomizer**가 나섭니다. "아, 이미지는 보안 검사 필요 없어. 그냥 지나가세요!"
//
//복잡한 필터를 거치지 않고 바로 이미지를 보여줍니다. (속도 향상)
//
//API 요청 (검문소 진입):
//
//사용자가 "게시글 목록 줘"(/api/v1/community)라고 요청합니다.
//
//이제 **filterChain**이라는 종합 검문소에 들어섭니다.
//
//        1차 관문 (CORS):
//
//        "너 어디서 왔어? localhost:3000(프론트엔드)에서 왔어? 어, 허용된 곳이네. 통과."
//
//        2차 관문 (CSRF & Session):
//
//        "우린 토큰 쓸 거니까 위조 방지(CSRF)는 끄고, 서버에 기억(Session)도 안 할 거야."
//
//        3차 관문 (JWT Filter):
//
//        **JwtFilter**가 ID/PW 검사 전에 먼저 나섭니다.
//
//"출입증(토큰) 내놔 봐. 유효하네? 너 인증된 사용자구나." (도장 쾅!)
//
//        4차 관문 (권한 확인):
//
//        "지금 가려는 곳이 /api/v1/community네? 여긴 누구나 갈 수 있어(permitAll). 지나가."
//
//만약 관리자 페이지였다면? "너 관리자 권한 있어?" 하고 확인했을 겁니다.
//
//거부 (Exception):
//
//만약 토큰이 없거나 이상하면? "돌아가(401)!"
//
//권한이 없으면? "출입 금지야(403)!" 하고 에러를 보냅니다.