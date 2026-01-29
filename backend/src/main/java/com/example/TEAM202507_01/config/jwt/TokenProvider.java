package com.example.TEAM202507_01.config.jwt; // 1. 파일의 패키지 위치입니다.

// 2. [Imports] JWT 처리, 암호화, 스프링 시큐리티 관련 도구들을 가져옵니다.
import com.example.TEAM202507_01.config.property.ErrorMessagePropertySource; // 에러 메시지 문구를 가져오는 설정 파일
import com.github.dockerjava.api.model.Secret;
import io.jsonwebtoken.*; // JWT를 만들고 파싱하는 라이브러리 (JJWT)
import io.jsonwebtoken.io.Decoders; // Base64 문자열을 바이트로 변환하는 도구
import io.jsonwebtoken.security.Keys; // 암호화 키를 안전하게 생성하는 도구
import io.jsonwebtoken.security.SecurityException; // 보안 관련 예외
import lombok.RequiredArgsConstructor; // final 변수용 생성자 자동 생성
import org.springframework.beans.factory.InitializingBean; // 빈 초기화 후 실행할 로직을 위한 인터페이스
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 인증 객체 구현체
import org.springframework.security.core.Authentication; // 인증 정보 인터페이스
import org.springframework.security.core.GrantedAuthority; // 권한 정보 인터페이스
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 권한 정보 구현체
import org.springframework.security.core.userdetails.User; // 스프링 시큐리티 기본 유저 객체
import org.springframework.stereotype.Component; // 스프링 빈으로 등록

import javax.crypto.SecretKey; // 암호화 키 타입
import java.security.Key; // 자바 표준 키 인터페이스
import java.util.Arrays; // 배열 유틸리티
import java.util.Collection; // 컬렉션 인터페이스
import java.util.Date; // 날짜 처리
import java.util.stream.Collectors; // 스트림 결과를 합치거나 변환할 때 사용

@Component // 3. "스프링아, 이 클래스는 네가 관리하는 부품(Bean)이야." 등록
@RequiredArgsConstructor // 4. final이 붙은 필드(jwtPropertySource, errorMessage...)를 채워주는 생성자를 자동으로 만듭니다.
public class TokenProvider implements InitializingBean { // 5. InitializingBean을 상속받아, 생성 후 키 세팅 작업을 수행합니다.

    // 6. JWT 설정값(비밀키, 유효기간 등)을 담고 있는 객체입니다.
    private final JwtPropertySource jwtPropertySource;

    // 7. 토큰 안에 '권한 정보'를 저장할 때 쓸 이름표(Key)입니다. (예: "authorities": "ROLE_USER")
    private static final String AUTH_CLAIM_KEY = "authorities";

    // 8. 실제 암호화/복호화에 사용할 비밀키 객체입니다. (아직은 null, 아래에서 채움)
    private Key key;

    // 9. 에러가 났을 때 보여줄 메시지들이 담긴 객체입니다.
    private final ErrorMessagePropertySource errorMessagePropertySource;

    /**
     * [초기화 메서드] 스프링이 이 빈을 생성하고 의존성 주입을 끝낸 직후에 실행됩니다.
     * 설정 파일에 있는 문자열 비밀키를 가져와서, 실제 암호화에 쓸 수 있는 Key 객체로 변환합니다.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 10. 설정 파일에 있는 비밀키(Secret)는 문자열(Base64) 상태입니다. 이걸 해독해서 바이트 배열로 만듭니다.
        byte[] bytes = Decoders.BASE64.decode(jwtPropertySource.getSecret());

        // 11. 바이트 배열을 기반으로 HMAC-SHA 암호화 알고리즘에 맞는 '진짜 키 객체'를 생성해서 key 변수에 저장합니다.
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * [토큰 생성] 로그인 성공 시 호출되어 엑세스 토큰(문자열)을 만듭니다.
     * @param authentication 현재 로그인한 사용자의 정보(아이디, 권한 등)가 들어있습니다.
     */
    public String createToken(Authentication authentication) {
        // 12. 사용자의 권한 목록을 가져옵니다. (예: [ROLE_USER, ROLE_ADMIN])
        // stream()을 돌려서 각 권한을 문자열로 바꾸고, 콤마(,)로 이어 붙입니다.
        // 결과 예시: "ROLE_USER,ROLE_ADMIN"
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 13. 현재 시간을 구합니다. (토큰 발급 시간)
        Date now = new Date();

        // 14. 현재 시간 + 설정 파일에 있는 유효기간(초)을 더해서 '만료 시간'을 계산합니다.
        Date expiration = new Date(now.getTime() + (jwtPropertySource.getExpirationSeconds())); // 만료일 계산

        // 15. JWT 빌더를 사용해 토큰을 만듭니다.
        return Jwts.builder()
                .issuedAt(now)                           // 16. 발급 시간 설정
                .issuer(jwtPropertySource.getIss())      // 17. 발급자 설정 (예: "MyCompany")
                .subject(authentication.getName())       // 18. 제목(Subject)에 사용자 아이디(로그인ID)를 넣습니다.
                .expiration(expiration)                  // 19. 만료 시간 설정
                .claim(AUTH_CLAIM_KEY, authorities)      // 20. 커스텀 데이터: 아까 만든 권한 문자열을 넣습니다.
                .signWith(key)                           // 21. 미리 만들어둔 비밀키(key)로 서명(암호화)합니다. ★제일 중요
                .compact();                              // 22. 위 내용들을 압축해서 최종 문자열로 반환합니다.
    }

    /**
     * [인증 정보 조회] 토큰을 해독해서 "이 사람이 누구고 권한이 뭔지" 객체로 만들어줍니다.
     * (필터에서 SecurityContext에 넣을 때 사용됨)
     */
    public Authentication getAuthentication(String token) {
        // 23. 토큰을 파싱(해독)해서 안에 있는 정보(Claims)를 꺼냅니다.
        // verifyWith(key): 비밀키로 서명이 맞는지 확인합니다.
        // getPayload(): 내용물을 가져옵니다.
        Claims claims = Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload();

        // 24. 꺼낸 정보 중 'authorities'(권한) 문자열을 콤마로 쪼갭니다.
        // 쪼갠 문자열들을 하나하나 SimpleGrantedAuthority 객체로 바꿔서 리스트로 만듭니다.
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTH_CLAIM_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // 25. 찾아낸 아이디(Subject), 비밀번호(토큰 기반이라 비번은 빈 문자열 "" 처리), 권한 목록으로
        // 스프링 시큐리티의 User 객체(Principal)를 만듭니다.
        User principal = new User(claims.getSubject(), "", authorities);

        // 26. 최종적으로 인증 객체(Authentication)를 만들어서 반환합니다.
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * [토큰 검증] 이 토큰이 유효한지 검사합니다.
     * (위조 여부, 만료 여부, 형식 오류 등)
     */
    public boolean isValidToken(String token) throws Exception {
        try {
            // 27. 토큰을 파싱해봅니다. 만약 문제가 있다면 여기서 에러(Exception)가 터집니다.
            // 문제가 없으면 정상적으로 파싱되고 다음 줄로 넘어갑니다.
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);

            // 28. 에러가 안 났으니 유효한 토큰입니다. true 반환.
            return true;

        } catch (SecurityException | MalformedJwtException e) {
            // 29. 서명이 잘못됐거나(위조), 토큰 형식이 깨진 경우 -> "잘못된 서명입니다" 에러를 던집니다.
            throw new Exception(errorMessagePropertySource.getInvalidSignature());
        }
        // 아래 주석 처리된 부분들은 더 세밀하게 에러를 잡고 싶을 때 풀어서 쓰면 됩니다.
        // 현재는 위조/형식 오류만 잡고 있네요.
//        catch (SecurityException | MalformedJwtException e) {
//            throw new Exception(errorMessagePropertySource.getInvalidSignature());
//        } catch (ExpiredJwtException e) {
//            throw new Exception(errorMessagePropertySource.getExpiredToken()); // 만료된 토큰
//        } catch (UnsupportedJwtException e) {
//            throw new Exception(errorMessagePropertySource.getUnsupportedToken()); // 지원하지 않는 토큰
//        } catch (IllegalArgumentException e) {
//            throw new Exception(errorMessagePropertySource.getInvalidToken()); // 값이 비었거나 이상함
//        }
    }

    /**
     * [아이디 추출] 토큰에서 사용자 아이디(Subject)만 쏙 꺼내는 보조 메서드입니다.
     */
    public String getLoginId(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key) // 30. 비밀키로 검증하고
                .build()
                .parseSignedClaims(token)    // 31. 토큰을 해석해서
                .getPayload()                // 32. 내용물을 꺼내고
                .getSubject();               // 33. 그 중 Subject(아이디)를 반환합니다.
    }
}
//
//개업 준비 (afterPropertiesSet):
//
//서버가 켜지면 스프링이 이 클래스를 만듭니다.
//
//이때 설정 파일(jwtPropertySource)에 적어둔 **비밀키(Secret Key)**를 가져와서, 암호화 작업에 쓸 수 있도록 **도장(Key 객체)**으로 깎아둡니다.
//
//출입증 발급 (createToken):
//
//사용자가 로그인을 성공합니다.
//
//서버는 이 클래스에게 **"user1(아이디)이고, 권한은 ADMIN이야. 이걸로 토큰 하나 만들어줘"**라고 시킵니다.
//
//이 클래스는 정보들을 암호화해서 긴 문자열(JWT)로 만들어 돌려줍니다.
//
//검문 (isValidToken):
//
//사용자가 "마이페이지 보여줘"라며 토큰을 내밉니다.
//
//이 클래스는 아까 만들어둔 **도장(Key)**을 대조해봅니다. "어? 서명이 안 맞네? 위조됐어!" 혹은 "유효기간 지났네!" 하고 판단합니다.
//
//신원 확인 (getAuthentication, getLoginId):
//
//        토큰이 정상이면, 그 안을 해독해서 "아, 얘 아이디가 user1이구나" 하고 정보를 꺼내서 스프링 시큐리티에게 알려줍니다.