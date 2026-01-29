package com.example.TEAM202507_01.config.property; // 1. 이 클래스가 위치한 패키지 주소입니다.

// 2. [Imports] 스프링의 설정 기능과 롬복 라이브러리를 가져옵니다.
import org.springframework.beans.factory.annotation.Value; // 설정 파일의 값을 변수에 주입해주는 어노테이션
import org.springframework.context.annotation.Configuration; // 이 클래스가 '설정 클래스'임을 알리는 어노테이션
import org.springframework.context.annotation.PropertySource; // 읽어올 외부 설정 파일의 위치를 지정하는 어노테이션

import lombok.Getter; // 변수값을 꺼낼 수 있게 get 메서드를 자동 생성해주는 롬복

@Configuration // 3. "스프링아, 이건 설정 정보를 담고 있는 중요한 클래스야. 서버 켜질 때 미리 챙겨둬(Bean 등록)."
@PropertySource(value = "classpath:error-messages.properties") // 4. "설정값들은 resources 폴더 안에 있는 'error-messages.properties' 파일에서 읽어와."
@Getter // 5. 아래 변수들의 값을 다른 클래스에서 꺼내 쓸 수 있게 getAlreadyExistedUser() 같은 메서드를 자동으로 만들어줍니다.
public class ErrorMessagePropertySource {

    // 6. [중복 유저 에러 메시지]
    // 설정 파일에서 'error.message.alreadyExistedUser'라고 적힌 줄을 찾아서 그 뒤에 있는 글자(예: "이미 가입된 유저입니다")를 가져옵니다.
    @Value("${error.message.alreadyExistedUser}")
    private String alreadyExistedUser; // 가져온 글자를 이 변수에 저장합니다.

    // 7. [유저 없음 에러 메시지]
    // 설정 파일에서 'error.message.userNotFound' 키의 값을 읽어옵니다. (예: "존재하지 않는 사용자입니다")
    @Value("${error.message.userNotFound}")
    private String userNotFound; // 변수에 저장

    // 8. [접근 금지 에러 메시지]
    // 설정 파일에서 'error.message.forbidden' 키의 값을 읽어옵니다. (예: "접근 권한이 없습니다")
    @Value("${error.message.forbidden}")
    private String forbidden; // 변수에 저장

    // 9. [서명 오류 에러 메시지]
    // JWT 토큰이 위조되었거나 서명이 깨졌을 때 보여줄 메시지입니다.
    @Value("${error.message.invalidSignature}")
    private String invalidSignature; // 변수에 저장

    // 10. [만료 토큰 에러 메시지]
    // JWT 토큰의 유효기간이 지났을 때 보여줄 메시지입니다.
    @Value("${error.message.expiredToken}")
    private String expiredToken; // 변수에 저장

    // 11. [지원 불가 토큰 에러 메시지]
    // 서버가 알 수 없는 형식의 토큰일 때 보여줄 메시지입니다.
    @Value("${error.message.unsupportedToken}")
    private String unsupportedToken; // 변수에 저장

    // 12. [잘못된 토큰 에러 메시지]
    // 토큰 값이 비었거나 이상할 때 보여줄 메시지입니다.
    @Value("${error.message.invalidToken}")
    private String invalidToken; // 변수에 저장

    // 13. [로그인 실패 에러 메시지]
    // 아이디는 맞는데 비밀번호가 틀렸을 때 주로 사용하는 메시지입니다.
    @Value("${error.message.badCredentials}")
    private String badCredentials; // 변수에 저장
}
//
//준비 (Server Start):
//
//서버가 부팅될 때 스프링이 이 클래스를 발견합니다.
//
//@PropertySource에 적힌 대로 error-messages.properties라는 텍스트 파일을 찾아 읽습니다.
//
//파일 안에 적힌 error.message.userNotFound=유저를 찾을 수 없습니다. 같은 내용을 읽어서, 자바 변수 userNotFound에 쏙 집어넣습니다.
//
//        상황 발생 (Runtime):
//
//사용자가 없는 아이디로 로그인을 시도했습니다.
//
//로그인 서비스는 "어? 유저가 없네?"라고 판단합니다.
//
//메시지 조회 (Use):
//
//서비스는 "에러 메시지 뭐였지?" 하고 이 클래스(ErrorMessagePropertySource)에게 물어봅니다.
//
//이 클래스는 아까 로딩해둔 **"유저를 찾을 수 없습니다"**라는 문장을 꺼내줍니다.
//
//응답 (Response):
//
//서버는 이 문장을 사용자 화면에 예쁘게 띄워줍니다.