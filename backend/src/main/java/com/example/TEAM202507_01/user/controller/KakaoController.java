package com.example.TEAM202507_01.user.controller;

import com.example.TEAM202507_01.config.jwt.TokenDto;
import com.example.TEAM202507_01.user.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    // [수정 포인트]
    // 1. @GetMapping -> @PostMapping (프론트가 POST로 보냄)
    // 2. "/kakao/callback" -> "/kakao" (프론트 요청 주소와 일치시킴)
    // 3. @RequestParam -> @RequestBody (JSON으로 데이터를 받음)
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        
        // 프론트엔드가 보낸 JSON { "code": "..." } 에서 코드 꺼내기
        String code = body.get("code");
        
        log.info("🔹 카카오 로그인 요청 들어옴! Code: {}", code);

        if (code == null) {
            return ResponseEntity.badRequest().body("카카오 인증 코드가 없습니다.");
        }

        try {
            // 서비스 로직 실행 (카카오 토큰 받기 -> 유저 정보 받기 -> JWT 발급)
            String jwtToken = kakaoService.kakaoLogin(code);

            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 처리에 실패했습니다. (토큰 없음)");
            }

            // 클라이언트에게 JWT 전달
            return ResponseEntity.ok(TokenDto.builder()
                    .token(jwtToken)
                    .build());

        } catch (Exception e) {
            log.error("🚨 카카오 로그인 실패 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }
}