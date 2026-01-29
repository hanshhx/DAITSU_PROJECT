package com.example.TEAM202507_01.user.service;

import com.example.TEAM202507_01.config.jwt.TokenProvider;
import com.example.TEAM202507_01.user.dto.CreateUserDto;
import com.example.TEAM202507_01.user.dto.UserDto;
import com.example.TEAM202507_01.user.dto.kakao.KakaoTokenResponse;
import com.example.TEAM202507_01.user.dto.kakao.KakaoUserInfo;
import com.example.TEAM202507_01.user.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final UserMapper userMapper;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${kakao.client-id}")
    private String clientId;

    // ✅ 리다이렉트 URI 고정 (포트 없음)
    private final String redirectUri = "http://localhost/sign-in/kakao/callback";

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    @Transactional
    public String kakaoLogin(String code) throws Exception {
        // 1. 토큰 받기
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2. 유저 정보 받기 (GET 방식으로 변경됨)
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);

        // 3. 회원가입/로그인 처리
        String kakaoLoginId = "kakao_" + kakaoUserInfo.getId(); 
        UserDto userDto = registerKakaoUserIfNeed(kakaoLoginId, kakaoUserInfo);

        log.info("🔹 카카오 로그인 처리된 유저: {}", userDto.getNickname());

        if (userDto.getId() == null) {
            return null;
        }

        // 4. 강제 로그인 및 JWT 발급
        Authentication authentication = forceLogin(userDto);
        return tokenProvider.createToken(authentication);
    }

    // --- 내부 메서드들 ---

    // 1. 토큰 요청
    private String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri); 
        params.add("code", code);

        try {
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    KakaoTokenResponse.class
            );
            return response.getBody().getAccessToken();
        } catch (HttpClientErrorException e) {
            log.error("🚨 카카오 토큰 요청 실패: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    // 2. 유저 정보 요청 (여기를 GET으로 수정!)
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 🔥 [핵심 수정] GET 방식은 Body가 필요 없습니다. 헤더만 보냅니다.
        HttpEntity<?> kakaoProfileRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                    userInfoUri, // https://kapi.kakao.com/v2/user/me
                    HttpMethod.GET, // 👈 POST -> GET 으로 변경!
                    kakaoProfileRequest,
                    KakaoUserInfo.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("🚨 유저 정보 요청 실패: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    // 3. 회원가입/조회 로직
    private UserDto registerKakaoUserIfNeed(String loginId, KakaoUserInfo kakaoUserInfo) throws Exception {
        UserDto existingUser = userMapper.findByLoginId(loginId);

        if (existingUser != null) {
            return existingUser;
        }

        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        boolean existingEmail = userService.checkEmail(email);

        if (existingEmail) {
            log.warn("🚨 이미 가입된 이메일입니다: {}", email);
            return new UserDto(); 
        }

        log.info("✨ 신규 카카오 회원가입 진행: {}", loginId);

        String nickname = kakaoUserInfo.getKakaoAccount().getProfile().getNickname();
        if (nickname == null) nickname = "UnknownUser";

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        String newUuid = UUID.randomUUID().toString();

        CreateUserDto newUser = new CreateUserDto();
        newUser.setId(newUuid);
        newUser.setLoginId(loginId);
        newUser.setPassword(encodedPassword);
        newUser.setName(nickname);
        newUser.setNickname(nickname);
        newUser.setEmail(email);

        newUser.setGender(null);
        newUser.setBirthDate(null);
        
        userMapper.save(newUser);
        userMapper.saveAuthority(newUser.getLoginId(), "ROLE_USER");

        UserDto returnDto = new UserDto();
        returnDto.setId(newUser.getId());
        returnDto.setLoginId(newUser.getLoginId());
        returnDto.setPassword(newUser.getPassword());
        returnDto.setName(newUser.getName());
        returnDto.setNickname(newUser.getNickname());
        returnDto.setEmail(newUser.getEmail());

        return returnDto;
    }

    // 4. 강제 로그인
    private Authentication forceLogin(UserDto userDto) {
        UserDetails principal = new User(userDto.getLoginId(), "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }
}