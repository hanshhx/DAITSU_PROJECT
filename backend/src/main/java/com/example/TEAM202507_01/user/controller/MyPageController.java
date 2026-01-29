package com.example.TEAM202507_01.user.controller;

import com.example.TEAM202507_01.user.dto.MyPageDto;
import com.example.TEAM202507_01.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // 추가됨
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // [수정 1] 에러를 던지는 대신 null을 반환하도록 변경
    private String getLoginId(UserDetails userDetails) {
        if (userDetails == null) {
            return null; // 로그인이 안 된 상태면 null 반환
        }
        return userDetails.getUsername();
    }

    @GetMapping("/info")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        
        return ResponseEntity.ok(myPageService.getMyInfo(userId));
    }

    @PutMapping("/info")
    public ResponseEntity<String> updateMyInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody MyPageDto dto) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        dto.setId(userId);
        myPageService.updateMyInfo(dto);
        return ResponseEntity.ok("수정완료");
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getMyPosts(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "1") int page) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        int size = 10;
        return ResponseEntity.ok(myPageService.getMyPosts(userId, (page-1)*size, size));
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getMyComments(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "1") int page) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        int size = 10;
        return ResponseEntity.ok(myPageService.getMyComments(userId, (page-1)*size, size));
    }

    // [수정 2] 병원 페이지 등에서 호출될 때 500 에러를 막는 핵심 부분
    @GetMapping("/favorites")
    public ResponseEntity<?> getMyFavorites(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "1") int page) {
        String userId = getLoginId(userDetails);
        
        // 로그인이 안 되어 있다면 에러(500) 대신 "로그인 필요(401)" 응답을 보냅니다.
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int size = 10;
        return ResponseEntity.ok(myPageService.getMyFavorites(userId, (page-1)*size, size));
    }

    @PutMapping("/post/{id}")
    public ResponseEntity<String> updatePost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Map<String, String> body) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        myPageService.updatePost(id, userId, body.get("title"), body.get("content"));
        return ResponseEntity.ok("수정완료");
    }

    @PutMapping("/comment/{id}")
    public ResponseEntity<String> updateComment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Map<String, String> body) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        myPageService.updateComment(id, userId, body.get("content"));
        return ResponseEntity.ok("수정완료");
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<String> deletePost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        myPageService.deletePost(id, userId);
        return ResponseEntity.ok("삭제완료");
    }

    @DeleteMapping("/comment/{id}")
    public ResponseEntity<String> deleteComment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        myPageService.deleteComment(id, userId);
        return ResponseEntity.ok("삭제완료");
    }

    @DeleteMapping("/favorite/{id}")
    public ResponseEntity<String> deleteFavorite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        String userId = getLoginId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        myPageService.deleteFavorite(id, userId);
        return ResponseEntity.ok("삭제완료");
    }
}