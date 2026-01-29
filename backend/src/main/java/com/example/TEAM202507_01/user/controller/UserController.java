package com.example.TEAM202507_01.user.controller;

import com.example.TEAM202507_01.config.helper.CookieHelper;
import com.example.TEAM202507_01.config.jwt.TokenDto;
import com.example.TEAM202507_01.config.security.CustomUserDetails;
import com.example.TEAM202507_01.menus.mailgun.dto.mailDto;
import com.example.TEAM202507_01.user.dto.*;
import com.example.TEAM202507_01.user.repository.UserMapper;
import com.example.TEAM202507_01.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final CookieHelper cookieHelper;
    private final UserMapper userMapper;

    // 1. 전체 회원 조회 (관리자용)
    @GetMapping
    public ResponseEntity<List<UserDto>> getUserList() {
        return ResponseEntity.ok(userService.findAll());
    }

    // 2. 회원가입
    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody CreateUserDto user) {
        userService.join(user);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/user/{id}").buildAndExpand(user.getLoginId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    // 3. 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> signIn(@RequestBody UserSignInDto signInDto) {
        String idInput = signInDto.getLoginId();
        if (idInput.matches("^.+\\s+.+$")) {
            idInput = idInput.replaceAll(" ", "" );
            signInDto.setLoginId(idInput);
        }
        String token = userService.createToken(signInDto);

        // 쿠키에 토큰 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Set-Cookie", cookieHelper.makeJwtCookie(token));

        return new ResponseEntity<>(TokenDto.builder().token(token).build(), httpHeaders, HttpStatus.OK);
    }

    // 4. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> signOut() {
        HttpHeaders httpHeaders = new HttpHeaders();
        cookieHelper.deleteJwtCookie(httpHeaders);
        return ResponseEntity.ok().headers(httpHeaders).body(null);
    }

    // 5. 회원 정보 조회
    @GetMapping("/{loginId}")
    public ResponseEntity<UserDto> retrieve(@PathVariable String loginId) {
        return ResponseEntity.ok(userService.findById(loginId));
    }

    // 6. 회원 정보 수정
    @PutMapping("/{loginId}")
    public ResponseEntity<Void> update(@PathVariable String loginId, @RequestBody UserDto user) {
        user.setLoginId(loginId);
        userService.update(user);
        return ResponseEntity.ok().build();
    }

    // 7. 회원 탈퇴
    @DeleteMapping("/{loginId}")
    public ResponseEntity<Void> delete(@PathVariable String loginId) {
        userService.delete(loginId);
        return ResponseEntity.ok().build();
    }
    //8. ID 찾기 토큰 발급
    @PostMapping("/find-id/get-token")
    public ResponseEntity<Void> getToken(@RequestParam String addr) {
        String tempToken = Integer.toString((int)Math.floor(Math.random()*1000000)+1);
        int tempLength = tempToken.length();
        for (int i =0;i<6-tempLength;i++){
            tempToken = "0"+tempToken;
        }
        String token = tempToken;

        userService.getTokenForFindID(addr, token);
        return ResponseEntity.ok().build();
    }
    //9. ID찾기
    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@RequestBody FindUserIdDto findUserIdDto) {
        String findId = userService.findUserId(findUserIdDto);
        String output = "";
        if (findId == null) {
            output = "이메일이 잘못 되었거나 가입된 아이디가 없습니다.";
        } else if (findId.equals("토큰 불일치")){
            output = "인증 번호가 다릅니다.";
        } else if (findId.equals("빈 토큰")){
            output = "인증 번호가 비어있습니다.";
        } else if (findId.equals("서버 오류 발생")){
            output = "서버 내부적 오류 발생 관리자 문의 바랍니다.";
        }else {
            output = findId;
        }
        if (output.matches("^kakao_[0-9].+")) {
            return ResponseEntity.ok("kakao");
        } else if (output.matches("^NAVER_[0-9A-Za-z_-]+$")) {
            return ResponseEntity.ok("naver");
        }else {

        return ResponseEntity.ok(output);
        }
    }

    //10, PW 리셋
    @PostMapping("/getResetPw")
    public ResponseEntity<Void> getResetPw(@RequestBody ResetPasswordDto resetPasswordDto) {
        userService.getResetPw(resetPasswordDto);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/resetPw")
    public ResponseEntity<Void> resetPw(@RequestParam String token , @RequestParam String email) {
      boolean isOk = userService.resetPw(token,email);
      if (isOk) {
      return ResponseEntity.ok().build();

      } else {
          return ResponseEntity.notFound().build();
      }
    }
    @PostMapping("/resetPw")
    public ResponseEntity<Void> resetPw(@RequestBody UpdatePwDto updatePwDto) {
        try {
            userService.updatePw(updatePwDto);
            return ResponseEntity.ok().build();

        }catch (RuntimeException e){
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/auth")
    public ResponseEntity<UserAuthDto> checkAuth(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        // 1. 토큰 검증 실패 또는 만료 시 null (SecurityFilter에서 걸러지지만 안전장치)
        if (customUserDetails == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 토큰에서 User ID(PK) 추출
        String userId = customUserDetails.getLoginId();

        // 3. DB에서 최신 유저 정보(권한 포함) 조회
        UserAuthDto userAuthInfo = userService.findUserAuthInfo(userId);

        // 4. 프론트엔드로 반환
        return ResponseEntity.ok(userAuthInfo);
    }

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam String loginId) {
        boolean isAvailable = userService.checkIdAvailability(loginId);
        return ResponseEntity.ok(isAvailable);
    }

    // 11. 이메일 인증 번호 발송 (POST /api/v1/user/checkemail/get-token)
    @PostMapping("/checkemail/get-token")
    public ResponseEntity<String> emailGetToken(@RequestBody CheckEmailDto checkEmailDto) {
        // 6자리 랜덤 숫자 생성 (String.format 사용으로 코드 간소화)
        // 0~999999 사이의 숫자를 뽑고, 빈 자리는 0으로 채움 (예: 5 -> "000005")
        String email = checkEmailDto.getEmail();
        String token = String.format("%06d", (int)(Math.random() * 1000000));

        try {
            userService.getTokenForCheckEmail(email, token);
            return ResponseEntity.ok("인증 번호가 발송되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 중복 이메일 등 에러 처리
        }
    }

    // 12. 이메일 인증 번호 확인 (POST /api/v1/user/checkemail)
    @PostMapping("/checkemail")
    public ResponseEntity<String> verifyEmail(@RequestBody CheckEmailDto checkEmailDto) {
        try {
            boolean isVerified = userService.verifyEmailToken(checkEmailDto);

            if (isVerified) {
                return ResponseEntity.ok("인증에 성공했습니다.");
            } else {
                return ResponseEntity.status(400).body("인증 번호가 일치하지 않습니다.");
            }
        } catch (RuntimeException e) {
            // "인증 시간이 만료되었습니다" 등의 메시지 반환
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}