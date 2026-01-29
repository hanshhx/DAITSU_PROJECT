package com.example.TEAM202507_01.config; // 1. 이 파일이 위치한 패키지 경로입니다.

// 2. [Imports] 설정에 필요한 도구들을 가져옵니다.
import lombok.RequiredArgsConstructor; // final 변수용 생성자 자동 생성 (여기선 딱히 final 필드가 없어서 필수까진 아니지만 관습적으로 사용됨)
import lombok.extern.slf4j.Slf4j; // 로그를 찍기 위한 도구 (log.info 사용 가능)
import org.springframework.beans.factory.annotation.Value; // 설정 파일(application.properties) 값을 가져옴
import org.springframework.context.annotation.Configuration; // "이건 설정 파일이야"라고 스프링에게 알림
import org.springframework.web.servlet.config.annotation.CorsRegistry; // CORS 설정 등록부
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // (현재는 안 쓰지만) 인터셉터 설정 도구
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // 정적 파일(이미지 등) 경로 설정 등록부
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // 웹 설정을 커스텀하기 위한 인터페이스

import java.nio.file.Path; // 파일 경로를 다루는 자바 표준 객체
import java.nio.file.Paths; // 경로 객체를 생성하는 도구

@Slf4j // 3. 로그 기록을 위해 log 변수를 자동으로 만들어줍니다.
@Configuration // 4. 스프링이 서버 켤 때 이 파일을 읽어서 설정을 적용하도록 합니다. (Bean 등록)
@RequiredArgsConstructor // 5. 롬복 생성자 주입 어노테이션입니다.
// 6. WebMvcConfigurer를 구현(implements)하여 스프링의 기본 웹 설정을 내 입맛대로 수정합니다.
public class WebMvcConfig implements WebMvcConfigurer {

    // 7. [이미지 경로 매핑] 브라우저의 URL 요청을 서버의 실제 파일 경로와 연결해주는 핵심 메서드입니다.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 8. [Path Setting] 프로젝트 루트(실행 위치)에 있는 "uploads" 폴더를 찾습니다.
        // .toAbsolutePath(): "uploads"라는 상대 경로를 "C:\Users\..uploads" 같은 절대 경로(진짜 주소)로 바꿉니다.
        // 이렇게 해야 윈도우(C:\), 맥/리눅스(/home/user) 어디서든 알아서 경로를 찾습니다.
        Path uploadDir = Paths.get("uploads").toAbsolutePath();

        // 9. [URI Conversion] 파일 경로를 브라우저가 이해할 수 있는 URI 문자열로 바꿉니다.
        // 예: "C:uploads" -> "file:///C:/uploads/" 형태로 바뀝니다.
        // 스프링에게 "이건 인터넷 주소가 아니라 파일 시스템 주소야"라고 알려주는 접두어(file:///)가 붙습니다.
        String uploadPath = uploadDir.toUri().toString();

        // 10. [Logging] 실제 매핑된 경로가 맞는지 콘솔에 찍어서 확인합니다. (디버깅용)
        log.info("🖼️ [Final] 이미지 경로 매핑: /images/** -> {}", uploadPath);

        // 11. [Registration] 실제 매핑 규칙을 등록합니다.
        registry.addResourceHandler("/images/**") // 브라우저가 "http://서버/images/어쩌구" 라고 요청하면
                .addResourceLocations(uploadPath); // 실제로는 "file:///C:/uploads/" 폴더 안에서 "어쩌구" 파일을 찾아서 줍니다.
    }

    // 12. [Value Injection] application.properties에서 'server.address' 값을 가져옵니다.
    // 만약 값이 없으면 기본값으로 'localhost'를 씁니다. (:localhost)
    @Value("${server.address:localhost}")
    String serverAddress;

    // 13. [CORS Setting] 프론트엔드 접속 허용 설정입니다. (이전 WebConfig와 같은 역할)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 14. 모든 주소(/**)에 대해서 규칙을 적용합니다.
        registry.addMapping("/**")
                // 15. 허용할 프론트엔드 주소 목록입니다.
                // - 로컬 환경(localhost:3000)
                // - 네트워크 환경(IP주소:3000) - 팀원끼리 테스트할 때 필요
                .allowedOrigins("http://localhost:3000", "http://" + serverAddress + ":3000")

                // 16. 모든 HTTP 메서드(GET, POST, PUT, DELETE 등)를 허용합니다. ("*"는 모두 다 라는 뜻)
                .allowedMethods("*")

                // 17. 모든 헤더 정보를 허용합니다. (토큰 등 특수 헤더 포함)
                .allowedHeaders("*")

                // 18. 쿠키나 인증 정보 포함을 허용합니다. (로그인 유지에 필수)
                .allowCredentials(true);
    }
}
//
//상황 1: 사용자가 프로필 사진을 볼 때
//
//사용자가 회원가입을 하면서 프로필 사진(myface.jpg)을 올렸습니다.
//
//이 파일은 서버 컴퓨터의 프로젝트 폴더 안 uploads 라는 폴더에 저장되었습니다. (예: C:\Users\Dev\Projectuploads\myface.jpg)
//
//웹사이트에서 이 사진을 보여주려 합니다. HTML 이미지 태그는 <img src="http://localhost:8080/images/myface.jpg">라고 요청합니다.
//
//원래 스프링은 /images라는 URL 요청이 오면 컨트롤러(코드)를 찾습니다. 하지만 코드가 아니라 파일을 찾아야 하죠.
//
//이때 **addResourceHandlers**가 나섭니다.
//
//        "잠깐! /images/로 시작하는 요청은 코드 찾지 말고, 저기 uploads 폴더로 가서 파일을 찾아봐!" 라고 연결해줍니다.
//
//덕분에 브라우저는 이미지를 정상적으로 불러와서 화면에 띄웁니다.
//
//        상황 2: 프론트엔드와 통신할 때
//
//프론트엔드(localhost:3000)가 백엔드(localhost:8080)에 데이터를 달라고 합니다.
//
//        **addCorsMappings**가 "3000번 포트 친구는 믿을만해. 통과시켜!"라고 허가증을 내줍니다.