package com.example.TEAM202507_01.config.helper; // 1. 이 클래스가 위치한 폴더 경로입니다.

// 2. [Imports] 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.config.jwt.JwtPropertySource; // JWT 설정값(쿠키 이름, 유효시간 등)이 들어있는 파일
import lombok.RequiredArgsConstructor; // final 변수용 생성자를 자동으로 만들어주는 롬복
import org.springframework.http.HttpHeaders; // HTTP 응답 헤더를 다루는 도구 (삭제할 때 씀)
import org.springframework.http.ResponseCookie; // 쿠키를 쉽게 만들게 도와주는 스프링 도구
import org.springframework.stereotype.Component; // 이 클래스를 스프링 빈(Bean)으로 등록

import java.util.Set;

@Component // 3. "스프링아, 이 클래스는 내가 직접 new 안 해도 네가 알아서 관리해줘"라고 등록합니다.
@RequiredArgsConstructor // 4. "final이 붙은 변수(jwtPropertySource)를 채워주는 생성자를 자동으로 만들어줘" (의존성 주입)
public class CookieHelper {

    // 5. [Dependency] 쿠키를 어떻게 만들지 설정값이 적혀있는 설정 파일입니다.
    // (예: 쿠키 이름은 'accessToken'이고, 유효기간은 3600초야... 같은 정보가 들어있음)
    private final JwtPropertySource jwtPropertySource;

    /**
     * [기능 1] 로그인 시: JWT 토큰을 담은 '보안 쿠키 문자열' 생성하기
     * @param jwt 실제 발급된 JWT 토큰 문자열
     * @return "Set-Cookie: accessToken=어쩌구; HttpOnly; ..." 형태의 문자열
     */
    public String makeJwtCookie(String jwt) {
        // 6. ResponseCookie 빌더를 사용하여 쿠키를 조립합니다.
        return ResponseCookie
                // 7. [Name & Value] 설정 파일에서 쿠키 이름(예: accessToken)을 가져오고, 값으로 jwt 토큰을 넣습니다.
                .from(jwtPropertySource.getCookieName(), jwt)

                // 8. [Security] httpOnly 설정 (true면 자바스크립트로 쿠키 접근 불가 -> 해킹 방지)
                .httpOnly(jwtPropertySource.isEnabledHttpOnly())

                // 9. [Security] secure 설정 (true면 https 통신에서만 쿠키 전송 -> 가로채기 방지)
                .secure(jwtPropertySource.isEnabledSecure())

                // 10. [Scope] path 설정 (보통 "/"로 설정하여 사이트 모든 곳에서 쿠키 사용 가능하게 함)
                .path(jwtPropertySource.getPath())

                // 11. [Expiration] maxAge 설정 (쿠키가 얼마나 살아있을지 초 단위로 설정)
                .maxAge(jwtPropertySource.getMaxAge())

                // 12. [Domain] 도메인 설정 (null이면 현재 도메인에서만 유효)
                .domain(null)

                // 13. [Security] sameSite 설정 (CSRF 공격 방지, Lax는 적당한 보안 수준)
                .sameSite("Lax")

                // 14. [Build] 설정한 내용으로 쿠키 객체를 완성합니다.
                .build()

                // 15. [Stringify] 쿠키 객체를 "Set-Cookie" 헤더에 들어갈 문자열로 변환하여 반환합니다.
                .toString();
    }

    /**
     * [기능 2] 로그아웃 시: 쿠키를 삭제하는(만료시키는) 명령어를 헤더에 추가하기
     * @param httpHeaders 응답으로 보낼 HTTP 헤더 객체 (여기에 삭제 명령을 적어넣음)
     */
    public void deleteJwtCookie(HttpHeaders httpHeaders) {
        // 16. 삭제용 쿠키 문자열을 만듭니다.
        String cookie = ResponseCookie
                // 17. [Name] 삭제할 쿠키와 '똑같은 이름'을 사용해야 브라우저가 알아먹습니다.
                // 값은 "" (빈 문자열)로 덮어씌웁니다.
                .from(jwtPropertySource.getCookieName(), "")

                // 18. [Path] 경로도 똑같아야 삭제됩니다.
                .path(jwtPropertySource.getPath())

                // 19. [Expiration] ★핵심★ 수명을 0초로 설정합니다. 브라우저가 받자마자 "어? 죽었네?" 하고 버립니다.
                .maxAge(0)

                // 20. 쿠키 객체 생성 및 문자열 변환
                .build()
                .toString();

        // 21. [Add Header] 완성된 '삭제용 쿠키'를 응답 헤더에 추가합니다.
        // "Set-Cookie"라는 이름표를 붙여서 보내면 브라우저가 처리합니다.
        httpHeaders.add("Set-Cookie", cookie);
    }
}
//
//로그인 성공 시 (makeJwtCookie):
//
//사용자가 아이디/비번을 입력하고 로그인에 성공했습니다.
//
//서버는 사용자의 신분증인 **JWT(토큰)**를 발급합니다.
//
//이때 이 헬퍼를 불러서 말합니다. "이 토큰, 안전한 쿠키로 포장해줘!"
//
//헬퍼는 설정 파일(JwtPropertySource)을 보고 "자바스크립트가 못 건드리게(HttpOnly), HTTPS에서만 작동하게(Secure), 7일 뒤에 상하게(MaxAge)" 옵션을 붙여서 Set-Cookie 문자열을 만들어 반환합니다.
//
//이 문자열은 응답 헤더에 실려 브라우저로 갑니다.
//
//        로그아웃 시 (deleteJwtCookie):
//
//사용자가 "로그아웃 할래요"라고 요청합니다.
//
//서버는 브라우저에게 쿠키를 삭제하라고 직접 명령할 수 없습니다. (브라우저 내부 파일이니까요)
//
//대신 이 헬퍼가 묘수를 씁니다. "똑같은 이름의 쿠키를 만들되, 유효기간을 0초로 설정해!"
//
//그리고 이 '0초짜리 쿠키'를 응답 헤더에 실어 보냅니다.
//
//브라우저는 이걸 받자마자 "어? 유효기간 끝났네?" 하고 기존 쿠키를 즉시 삭제합니다.