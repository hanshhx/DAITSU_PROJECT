package com.example.TEAM202507_01.config.jwt; // 1. 이 파일이 위치한 패키지 경로입니다.

// 2. [Imports] 필요한 도구들을 가져옵니다.
import lombok.Getter; // 변수들의 값을 꺼낼 수 있게 get 메서드를 자동 생성해주는 롬복
import org.springframework.beans.factory.annotation.Value; // 설정 파일의 값을 변수에 넣어주는 어노테이션
import org.springframework.context.annotation.Configuration; // 이 클래스가 설정 정보 파일임을 알리는 어노테이션
import org.springframework.context.annotation.PropertySource; // 읽어올 설정 파일의 위치를 지정하는 어노테이션

@Configuration // 3. "스프링아, 이건 설정(Config) 클래스야. 서버 켜질 때 미리 챙겨둬." (Bean 등록)
@PropertySource(value = "classpath:application.properties") // 4. "설정값들은 resources 폴더의 application.properties 파일에서 찾아와."
@Getter // 5. 아래 변수들(secret, iss 등)을 다른 클래스에서 읽을 수 있게 getSecret(), getIss() 등을 자동으로 만들어줍니다.
public class JwtPropertySource {

    // --- [JWT 관련 설정값] ---

    @Value("${jwt.secret}") // 6. 설정 파일에서 'jwt.secret'이라는 키를 찾아서 아래 변수에 넣습니다.
    private String secret; // 토큰을 암호화할 때 쓸 비밀키 (가장 중요!)

    @Value("${jwt.iss}") // 7. 설정 파일에서 'jwt.iss' (발행자, Issuer) 키를 찾아서 넣습니다.
    private String iss; // 이 토큰을 누가 발행했는지 적는 칸 (예: "TeamProject")

    @Value("${jwt.expiration-milliseconds}") // 8. 설정 파일에서 토큰 유효기간(밀리초 단위) 값을 가져옵니다.
    private long expirationSeconds; // 변수명은 Seconds지만, 실제로는 설정 파일 키 이름대로 밀리초가 들어갑니다. (토큰 수명)

    // --- [쿠키 관련 설정값] ---

    @Value("${cookie.jwt.name}") // 9. 브라우저에 저장할 쿠키의 이름 (예: "accessToken")을 가져옵니다.
    private String cookieName; // 쿠키의 이름표

    @Value("${cookie.jwt.http-only}") // 10. 쿠키의 HttpOnly 옵션 (true/false) 값을 가져옵니다.
    private boolean isEnabledHttpOnly; // true면 자바스크립트로 쿠키 탈취 불가능 (보안 강화)

    @Value("${cookie.jwt.secure}") // 11. 쿠키의 Secure 옵션 (true/false) 값을 가져옵니다.
    private boolean isEnabledSecure; // true면 https 프로토콜에서만 쿠키 전송 (보안 강화)

    @Value("${cookie.jwt.path}") // 12. 쿠키가 유효한 경로 설정 ("/" 면 사이트 전체)을 가져옵니다.
    private String path; // 쿠키가 사용될 범위

    @Value("${cookie.jwt.maxAge}") // 13. 쿠키의 수명(초 단위)을 가져옵니다.
    private long maxAge; // 쿠키가 브라우저에 살아있는 시간 (보통 토큰 유효기간과 맞춤)
}