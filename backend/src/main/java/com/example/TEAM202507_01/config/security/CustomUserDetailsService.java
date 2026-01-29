package com.example.TEAM202507_01.config.security; // 1. 이 파일이 위치한 패키지 주소입니다.

// 2. [Imports] 필요한 클래스들을 가져옵니다.
import com.example.TEAM202507_01.user.dto.UserDto; // DB에서 꺼내온 실제 유저 데이터
import com.example.TEAM202507_01.user.repository.UserMapper; // DB에 SQL을 날려줄 매퍼 인터페이스
import lombok.RequiredArgsConstructor; // final 변수 생성자 자동 생성 (의존성 주입용)
import org.springframework.security.core.userdetails.UserDetails; // 스프링 시큐리티 표준 유저 정보 인터페이스
import org.springframework.security.core.userdetails.UserDetailsService; // "유저 정보를 불러오는 서비스는 이래야 해"라는 표준 인터페이스
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 유저가 없을 때 던지는 표준 에러
import org.springframework.stereotype.Service; // "이건 비즈니스 로직을 수행하는 서비스야" (Bean 등록)

@Service // 3. 스프링에게 "이 클래스는 유저 정보를 가져오는 중요한 일꾼(Bean)이니까 네가 관리해"라고 등록합니다.
@RequiredArgsConstructor // 4. final이 붙은 필드(userMapper)를 초기화해주는 생성자를 자동으로 만들어줍니다.
// 5. [Class Definition] UserDetailsService 인터페이스를 구현합니다.
// 즉, "스프링 시큐리티야, 앞으로 유저 정보 필요하면 나(CustomUserDetailsService)한테 물어봐!"라고 선언하는 것입니다.
public class CustomUserDetailsService implements UserDetailsService {

    // 6. [Dependency] DB와 대화할 수 있는 도구(Mapper)를 가져옵니다.
    // 생성자 주입(@RequiredArgsConstructor)을 통해 스프링이 알아서 넣어줍니다.
    private final UserMapper userMapper;

    // 7. [Override Method] 스프링 시큐리티가 유저 정보를 달라고 할 때 호출하는 약속된 메서드입니다.
    // username: 사용자가 로그인할 때 입력한 아이디(로그인 ID)가 들어옵니다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 8. [DB Query] 매퍼에게 시켜서 DB를 조회합니다.
        // "SELECT * FROM users WHERE login_id = 'username'" 같은 쿼리가 실행됩니다.
        UserDto userDto = userMapper.findByLoginId(username);

        // 9. [Check Null] 만약 DB에서 아무것도 안 나왔다면? (없는 아이디라면)
        if (userDto == null) {
            // 10. "그런 사람 없습니다"라고 예외를 던집니다.
            // 이렇게 하면 스프링 시큐리티가 알아서 로그인 실패 처리를 합니다.
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        // 11. [Wrap & Return] 유저를 찾았다면, 바로 리턴하지 않고 '포장'을 합니다.
        // 스프링 시큐리티는 'UserDto'를 모르고 'UserDetails'만 알아듣기 때문에,
        // 아까 만든 어댑터(CustomUserDetails)에 UserDto를 쏙 넣어서 반환합니다.
        return new CustomUserDetails(userDto);
    }
}
//
//명령 하달: 스프링 시큐리티(인증 관리자)가 로그인 요청을 받습니다. "아이디가 user1이라는데, 얘 진짜 우리 회원 맞아? 정보 좀 가져와 봐."
//
//수색 시작 (loadUserByUsername):
//
//이 서비스(CustomUserDetailsService)가 호출됩니다.
//
//        "잠시만요, 제가 UserMapper한테 시켜서 DB 뒤져볼게요."
//
//DB 조회: userMapper.findByLoginId("user1")를 실행해서 실제 데이터베이스 테이블을 조회합니다.
//
//검증 및 포장:
//
//없으면: "그런 사람 없는데요?" 하고 에러(UsernameNotFoundException)를 던져서 로그인을 실패시킵니다.
//
//있으면: DB에서 가져온 날것의 데이터(UserDto)를, 아까 만들었던 스프링 시큐리티 전용 신분증(CustomUserDetails)에 예쁘게 포장합니다.
//
//        보고: 포장된 신분증을 스프링 시큐리티에게 "여기 있습니다!" 하고 건네줍니다. 그러면 스프링 시큐리티가 비밀번호를 대조하고 로그인을 시켜줍니다.