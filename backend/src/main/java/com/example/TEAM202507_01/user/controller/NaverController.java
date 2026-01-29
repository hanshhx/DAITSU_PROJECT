package com.example.TEAM202507_01.user.controller;

import com.example.TEAM202507_01.config.jwt.TokenDto;
import com.example.TEAM202507_01.user.service.NaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ★ 로그 기능 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j // ★ 로그 어노테이션
@RestController
@RequestMapping("/api/v1/auth/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;

    @PostMapping("/login")
    public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> request) {
        log.info("=================================================");
        log.info("🔹 [NaverController] 네이버 로그인 요청 들어옴!");

        String code = request.get("code");
        String state = request.get("state");

        log.info("   > 인가 코드 (Code): {}", code);
        log.info("   > 상태 값 (State): {}", state);

        try {
            // 1. 인가 코드로 네이버 액세스 토큰 발급
            String accessToken = naverService.getAccessToken(code, state);
            log.info("   > 액세스 토큰 발급 성공: {}", accessToken != null ? "O" : "X");

            // 2. 액세스 토큰으로 사용자 정보 조회 및 자체 JWT 발급
            TokenDto jwtToken = naverService.loginWithNaver(accessToken);
            log.info("✅ [NaverController] 로그인 성공! JWT 반환함.");
            log.info("=================================================");

            return ResponseEntity.ok(jwtToken);

        } catch (Exception e) {
            log.error("❌ [NaverController] 로그인 중 에러 발생", e);
            return ResponseEntity.status(500).body("네이버 로그인 실패: " + e.getMessage());
        }
    }
}