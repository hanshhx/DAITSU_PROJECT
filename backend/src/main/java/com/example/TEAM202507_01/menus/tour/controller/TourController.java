package com.example.TEAM202507_01.menus.tour.controller;

import com.example.TEAM202507_01.config.security.CustomUserDetails;
import com.example.TEAM202507_01.menus.tour.dto.TourDto;
import com.example.TEAM202507_01.menus.tour.service.TourService;
import com.example.TEAM202507_01.user.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import java.util.List;

// @RestController: 결과값으로 HTML이 아니라 JSON 데이터 자체를 반환하는 컨트롤러임.
// @RequestMapping("/api/v1/tour"): 이 컨트롤러의 모든 주소는 /api/v1/tour 로 시작함.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tour")
public class TourController {

    private final TourService tourService; // 서비스 연결
    private final FavoriteService favoriteService;

    // 1. 목록 조회 (GET /api/v1/tour)
    @GetMapping
    public ResponseEntity<List<TourDto>> getTourList() {
        // 서비스한테 목록 달라고 하고 200 OK와 함께 반환함.
        return ResponseEntity.ok(tourService.findAll());
    }

    // 2. 상세 조회 (GET /api/v1/tour/{id})
    // @PathVariable: URL 경로에 있는 숫자(id)를 변수로 가져옴.
    @GetMapping("/{id}")
    public ResponseEntity<TourDto> getTourDetail(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.findById(id));
    }

    // 3. 등록 및 수정 (POST /api/v1/tour)
    // @RequestBody: 사용자가 보낸 JSON 데이터를 TourDto 객체로 변환해서 받음.
    @PostMapping
    public ResponseEntity<TourDto> createTour(@RequestBody TourDto tour) {
        return ResponseEntity.ok(tourService.save(tour));
    }

    // 4. 삭제 (DELETE /api/v1/tour/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable Long id) {
        tourService.delete(id); // 삭제 시킴
        return ResponseEntity.ok("관광지 삭제 성공"); // 성공 메시지 반환
    }

    // 5. 즐겨찾기 토글
    @PostMapping("/{id}/favorite")
    public ResponseEntity<String> tourFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails // 🔥 현재 로그인한 사용자 정보
    ) {
        // 로그인 안 했으면 401 에러 반환
        if (userDetails == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String userId = userDetails.getId();
        // 즐겨찾기 서비스 호출 (카테고리, 유저ID, 식당ID)
        favoriteService.toggleFavorite("TOURS", userId, id);

        return ResponseEntity.ok("즐겨찾기 처리가 완료되었습니다.");
    }
}
