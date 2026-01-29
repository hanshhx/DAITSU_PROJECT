package com.example.TEAM202507_01.config; // 1. 이 설정 파일이 위치한 패키지 경로입니다.

// 2. [Imports] 설정에 필요한 스프링 프레임워크 도구들을 가져옵니다.
import org.springframework.beans.factory.annotation.Value; // 설정 파일(application.properties)의 값을 가져오는 도구
import org.springframework.context.annotation.Configuration; // 이 클래스가 '설정 파일'임을 알리는 어노테이션
import org.springframework.web.servlet.config.annotation.CorsRegistry; // CORS 규칙을 등록할 레지스트리(장부)
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // 웹 설정을 커스터마이징 할 수 있게 해주는 인터페이스

@Configuration // 3. "스프링아, 이건 그냥 자바 코드가 아니라 설정(Config) 클래스야. 서버 켤 때 적용해줘." (Bean 등록)
// 4. WebMvcConfigurer를 구현(implements)해서 "기본 웹 설정에 내가 원하는 규칙을 추가할게"라고 선언합니다.
public class WebConfig implements WebMvcConfigurer {

    // 5. [Value Injection] application.properties 파일에 있는 'server.address' 값을 가져옵니다.
    // 예: 192.168.0.10 (개발팀원끼리 테스트할 때 내 IP주소)
    // 이 값이 있어야 내 컴퓨터뿐만 아니라 같은 와이파이를 쓰는 팀원도 내 서버에 접속할 수 있습니다.
    @Value("${server.address}")
    String serverAddress;

    // 6. [Override] WebMvcConfigurer가 제공하는 'CORS 설정 추가 메서드'를 재정의합니다.
    // registry: 허용할 규칙들을 적어넣을 출입국 관리 장부입니다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // 7. [Mapping Path] 어떤 주소로 들어오는 요청을 허용할지 정합니다.
        // "/**" : 내 서버의 모든 API 주소(로그인, 게시판, 마이페이지 등 전부)에 대해 이 규칙을 적용하겠다는 뜻입니다.
        registry.addMapping("/**")

                // 8. [Allowed Origins] "누구의 접속을 허용할래?" (가장 중요한 부분!)
                // - "http://localhost:3000": 내 컴퓨터에서 띄운 리액트(프론트엔드) 허용
                // - "http://"+serverAddress+":3000": 팀원이 내 IP로 띄운 리액트 접속 허용
                // 만약 이걸 안 적어주면 프론트엔드에서 API 요청 시 무조건 에러가 납니다.
                .allowedOrigins("http://localhost:3000", "http://" + serverAddress + ":3000")

                // 9. [Allowed Methods] "어떤 방식의 요청을 허용할래?"
                // - GET: 데이터 조회, POST: 저장, PUT: 수정, DELETE: 삭제
                // - OPTIONS: 브라우저가 "통신 가능한가요?" 하고 찔러보는 예비 요청 (필수 허용)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // 10. [Allow Credentials] "쿠키나 인증 헤더(Authorization)를 주고받아도 돼?" -> 네(true)
                // 이게 true여야 로그인 후에 발급받은 쿠키(토큰)를 프론트엔드가 주고받을 수 있습니다.
                // 만약 false면 로그인이 되어도 쿠키가 전달되지 않아 로그인이 풀려버립니다.
                .allowCredentials(true);
    }
}
//
//차단 위기:
//
//프론트엔드(localhost:3000)에서 로그인 요청을 보냅니다.
//
//브라우저는 "어? 너네 포트 번호가 다르네? 이거 해킹 아니야?" 하고 일단 의심합니다.
//
//설정 확인:
//
//이때 서버(스프링 부트)는 이 WebConfig 설정을 참고합니다.
//
//        "잠깐, 설정 보니까 localhost:3000은 허용된 출처(Origin)라고 적혀있네."
//
//허가 (Header 추가):
//
//서버는 응답을 보낼 때 헤더에 Access-Control-Allow-Origin: http://localhost:3000이라는 **'입국 허가 도장'**을 찍어서 보냅니다.
//
//통신 성공:
//
//브라우저는 그 도장을 보고 "아, 허락받은 애구나" 하고 데이터를 프론트엔드에게 안전하게 넘겨줍니다.
//
//만약 이 설정이 없다면 브라우저 콘솔에 빨간색 CORS 에러가 뜨면서 통신이 실패합니다.