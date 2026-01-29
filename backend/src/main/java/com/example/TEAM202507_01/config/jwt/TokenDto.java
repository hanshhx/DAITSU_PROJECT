package com.example.TEAM202507_01.config.jwt; // 1. 이 파일이 위치한 폴더 경로(패키지)입니다.

// 2. [Imports] 코드를 줄여주는 마법의 도구 'Lombok' 라이브러리들을 가져옵니다.
import lombok.AllArgsConstructor; // 모든 필드를 넣는 생성자 자동 생성
import lombok.Builder;            // 객체를 예쁘게 조립하는 빌더 패턴 자동 생성
import lombok.Data;               // Getter, Setter, toString 등 필수 기능 자동 생성
import lombok.NoArgsConstructor;  // 빈 생성자(기본 생성자) 자동 생성

@Data // 3. 이 클래스 안의 모든 변수에 대해 Getter(꺼내기), Setter(넣기), toString(출력하기) 기능을 자동으로 만들어줍니다.
@Builder // 4. 객체를 생성할 때 'TokenDto.builder().accessToken("...").build()' 처럼 직관적으로 만들 수 있게 해줍니다.
@NoArgsConstructor  // 5. [중요] 파라미터가 없는 기본 생성자 'new TokenDto()'를 만들어줍니다.
// ★ 이 어노테이션이 있어야 하는 이유:
// 나중에 JSON 데이터를 자바 객체로 바꿀 때(Jackson 라이브러리 등),
// 일단 빈 깡통 객체(new TokenDto())를 먼저 만들고 나서 데이터를 채우기 때문에 이게 없으면 에러가 날 수 있습니다.
@AllArgsConstructor // 6. 모든 필드(변수)를 한 번에 다 채우는 생성자를 만들어줍니다. (@Builder가 이걸 필요로 합니다.)
public class TokenDto {

    // 7. [Field] 일반적인 토큰 문자열을 담는 공간입니다.
    // (보통은 accessToken과 역할이 겹치지만, 상황에 따라 통합 토큰값으로 쓰기도 합니다.)
    private String token;

    // 8. [Field] 인증 타입을 명시합니다.
    // 보통 "Bearer"라는 문자열이 들어갑니다.
    // 프론트엔드가 헤더에 넣을 때 "Authorization: Bearer <토큰>" 형태로 쓰라는 힌트입니다.
    private String grantType;       // 예: "Bearer"

    // 9. [Field] ★ 가장 중요한 실제 '출입증'입니다.
    // 사용자가 서버에 요청을 보낼 때마다 이 값을 보여줘야 통과됩니다.
    private String accessToken;     // 액세스 토큰

    // 10. [Field] 토큰이 언제 만료되는지 알려주는 시간 정보입니다.
    // 예: 3600000 (1시간 뒤 만료됨을 밀리초로 표시). 프론트엔드가 "언제 로그아웃 시킬지" 알 수 있게 해줍니다.
    private Long tokenExpiresIn;    // 만료 시간

    // 11. [Field] 액세스 토큰이 만료되었을 때, 로그인 없이 새 걸로 바꾸기 위한 '재발급권'입니다.
    // 유효기간이 액세스 토큰보다 훨씬 깁니다 (예: 7일, 30일).
    private String refreshToken;
}

//
//로그인 성공: 서버가 아이디/비번을 확인하고 "합격!" 판정을 내립니다.
//
//토큰 발행: TokenProvider가 윙윙 돌아가면서 **Access Token(출입증)**과 **Refresh Token(재발급권)**을 생성합니다.
//
//포장 (TokenDto):
//
//생성된 토큰 문자열들을 그냥 던져주지 않습니다.
//
//이 TokenDto라는 택배 상자를 하나 조립(Builder)합니다.
//
//상자 안에 accessToken, refreshToken, grantType("Bearer"), 만료시간을 차곡차곡 담습니다.
//
//배송:
//
//컨트롤러는 이 꽉 찬 TokenDto 상자를 JSON 형식({ "accessToken": "...", ... })으로 변환해서 프론트엔드에게 응답으로 보냅니다.
//
//        수령:
//
//프론트엔드는 이 상자를 받아서 열어보고, 토큰을 꺼내서 저장해둡니다.