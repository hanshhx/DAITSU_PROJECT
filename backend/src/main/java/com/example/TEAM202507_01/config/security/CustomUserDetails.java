package com.example.TEAM202507_01.config.security; // 1. 이 파일이 위치한 패키지 주소입니다.

// 2. [Imports] 필요한 클래스들을 가져옵니다.
import com.example.TEAM202507_01.user.dto.UserDto; // 우리가 만든 진짜 유저 데이터 객체
import lombok.Getter; // 롬복: getXxx() 메서드 자동 생성
import org.springframework.security.core.GrantedAuthority; // 권한 정보를 담는 표준 인터페이스
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 권한 정보를 구현한 객체 (문자열 권한)
import org.springframework.security.core.userdetails.UserDetails; // 스프링 시큐리티가 "유저란 이래야 해"라고 정해둔 표준 인터페이스

import java.util.ArrayList; // 리스트 기능을 쓰기 위함
import java.util.Collection; // 권한 목록을 담을 컬렉션 타입

@Getter // 3. 이 클래스 안의 필드(userDto 등)를 꺼낼 수 있게 getter 메서드를 자동으로 만들어줍니다.
// 4. [Class Definition] UserDetails 인터페이스를 '구현(implements)'합니다.
// 즉, "나는 이제부터 스프링 시큐리티의 표준 유저 역할을 할 거야"라고 선언하는 겁니다.
public class CustomUserDetails implements UserDetails {

    // 5. [Field] 우리의 진짜 유저 정보를 담을 그릇입니다.
    // 스프링 시큐리티는 이걸 직접 모르지만, 우리가 필요해서 품고 있는 겁니다.
    private final UserDto userDto; // 우리의 진짜 유저 정보


    // 6. [Constructor] 생성자입니다.
    // 이 객체를 만들 때, 외부(UserDetailsService)에서 진짜 유저 정보(UserDto)를 넣어주면 그걸 저장합니다.
    public CustomUserDetails(UserDto userDto) {
        this.userDto = userDto;
    }

    // 7. [Custom Method] 🔥 핵심 기능 1
    // UserDetails에는 없지만, 우리 프로젝트 편의를 위해 만든 메서드입니다.
    // 나중에 컨트롤러에서 로그인한 사람의 DB 고유 번호(PK)를 바로 꺼내려고 만듭니다.
    public String getId() {
        return userDto.getId(); // UserDto 안에 있는 진짜 ID를 반환
    }

    // 8. [Custom Method] 핵심 기능 2
    // 로그인 아이디(예: user123)를 꺼내는 편의 메서드입니다.
    public String getLoginId() {
        return userDto.getLoginId(); // UserDto 안의 로그인 아이디 반환
    }


    // --- [Override Methods] 아래는 UserDetails 인터페이스가 강제로 만들라고 시킨 메서드들입니다. ---

    // 9. [권한 목록 반환] "이 유저는 무슨 권한(Role)을 갖고 있니?"라고 스프링이 물어볼 때 씁니다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 10. 권한들을 담을 빈 리스트를 하나 만듭니다.
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 11. [Role Setting] 일단 모든 유저에게 "ROLE_USER"라는 권한을 줍니다. (하드코딩)
        // 만약 DB에 관리자 여부(role) 컬럼이 있다면, userDto.getRole() 해서 동적으로 넣으면 됩니다.
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 12. 만든 권한 리스트를 스프링에게 제출합니다.
        return authorities;
    }

    // 13. [비밀번호 반환] "이 유저의 비밀번호가 뭐니?" (로그인 검사할 때 씀)
    @Override
    public String getPassword() {
        return userDto.getPassword(); // 우리 DTO에 들어있는 암호화된 비밀번호를 줍니다.
    }

    // 14. [아이디 반환] "이 유저를 식별할 이름(아이디)이 뭐니?"
    @Override
    public String getUsername() {
        // 주의: 스프링은 메서드 이름이 getUsername이지만, 우리는 로그인 아이디를 의미합니다.
        // userDto에 있는 로그인용 아이디를 연결해줍니다.
        return userDto.getLoginId(); // 로그인 아이디 (변수명 확인: userId or email)
    }

    // --- [Status Checks] 계정 상태 확인 메서드들 (true: 정상, false: 사용불가) ---
    // 현재 프로젝트에서는 별도의 정지/잠금 로직이 없으므로 전부 무조건 OK(true)로 설정합니다.

    // 15. "계정이 만료되지 않았니?" -> 네(true), 만료 안 됐습니다.
    @Override
    public boolean isAccountNonExpired() { return true; }

    // 16. "계정이 잠겨있지 않니?" -> 네(true), 안 잠겼습니다.
    @Override
    public boolean isAccountNonLocked() { return true; }

    // 17. "비밀번호가 만료되지 않았니?" -> 네(true), 아직 쓸만합니다.
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 18. "이 계정 활성화된 거 맞니?" -> 네(true), 사용 가능합니다.
    @Override
    public boolean isEnabled() { return true; }

}
//
//로그인 시도: 사용자가 아이디/비번을 입력합니다.
//
//DB 조회: 서버는 DB에서 UserDto 정보를 가져옵니다. (이건 우리 서버만 아는 데이터 형식)
//
//변신 (Wrapping):
//
//스프링 시큐리티에게 이 정보를 넘겨줘야 하는데, 스프링은 UserDto를 모릅니다.
//
//그래서 CustomUserDetails라는 껍데기를 만들고, 그 안에 UserDto를 쏙 집어넣습니다.
//
//이제 이 객체는 겉으로는 스프링 표준(UserDetails)을 따르고, 속에는 진짜 정보(UserDto)를 품고 있게 됩니다.
//
//검사 및 저장:
//
//스프링 시큐리티는 이 CustomUserDetails를 보고 "아, 아이디는 이거고, 비번은 이거구나" 하고 검사를 마칩니다.
//
//검사가 끝나면 Authentication 안에 보관됩니다.
//
//활용 (Controller):
//
//나중에 컨트롤러에서 "지금 로그인한 사람의 DB 번호(PK)가 뭐야?" 할 때, ((CustomUserDetails) principal).getId() 처럼 껍데기를 열고 속 알맹이 데이터를 꺼내 쓸 수 있습니다.