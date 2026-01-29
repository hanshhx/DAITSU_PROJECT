package com.example.TEAM202507_01.menus.restaurant.controller;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 컨트롤러' 폴더에 위치한다는 주소입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.config.security.CustomUserDetails; // 로그인한 사용자 정보를 담고 있는 객체
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantBlogDto; // 블로그 검색 결과를 담을 가방
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // 맛집 정보를 담을 가방
import com.example.TEAM202507_01.menus.restaurant.service.RestaurantBlogService; // 블로그 검색 담당자
import com.example.TEAM202507_01.menus.restaurant.service.RestaurantCrawlerService; // 데이터 크롤링(수집) 담당자
import com.example.TEAM202507_01.menus.restaurant.service.RestaurantService; // 맛집 기본 업무(조회/저장) 담당자
import com.example.TEAM202507_01.user.service.FavoriteService; // 즐겨찾기 담당자
import org.springframework.http.ResponseEntity; // 응답(200 OK 등) 포장지
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 현재 로그인한 사람 찾는 도구
import org.springframework.transaction.annotation.Transactional; // (컨트롤러에는 보통 안 쓰지만 임포트되어 있네요)
import org.springframework.web.bind.annotation.*; // 웹 요청 처리 도구들

import lombok.RequiredArgsConstructor; // 생성자 자동 생성 도구
import java.util.List; // 리스트 도구

@RestController
// [3] 어노테이션(@RestController): "스프링아, 나는 화면(HTML)이 아니라 데이터(JSON)를 돌려주는 컨트롤러야."

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final이 붙은 필드들을 자동으로 초기화해주는 생성자를 만들어줍니다. (의존성 주입)

@RequestMapping("/api/v1/restaurant")
// [5] 주소 설정: "이 컨트롤러는 '/api/v1/restaurant'로 시작하는 모든 요청을 처리해."
// 예: localhost:8080/api/v1/restaurant/...
public class RestaurantController {

    // [6] 의존성 주입 (Dependency Injection)
    // 이 컨트롤러는 혼자 일하지 않고, 아래 4명의 전문 직원(Service)을 부려서 일을 처리합니다.
    private final RestaurantService restaurantService;       // 기본 CRUD (조회, 저장, 삭제) 담당
    private final RestaurantCrawlerService crawlerService;   // 데이터 수집 (공공데이터, 이미지) 담당
    private final RestaurantBlogService blogService;         // 블로그 리뷰 검색 담당
    private final FavoriteService favoriteService;           // 즐겨찾기(찜) 담당

    // ==========================================
    // 1. 맛집 목록 조회
    // ==========================================
    // 요청: GET /api/v1/restaurant
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getRestaurantList() {
        // [설명] 서비스에게 "모든 맛집 리스트 가져와"라고 시키고, 결과를 200 OK와 함께 반환합니다.
        return ResponseEntity.ok(restaurantService.findAll());
    }

    // ==========================================
    // 2. 맛집 상세 조회
    // ==========================================
    // 요청: GET /api/v1/restaurant/1 (예: 1번 식당 보여줘)
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantDetail(@PathVariable Long id) {
        // [설명] 주소창의 숫자({id})를 받아서, 서비스에게 "이 번호 식당 상세 정보 줘"라고 합니다.
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    // ==========================================
    // 3. 맛집 수동 등록
    // ==========================================
    // 요청: POST /api/v1/restaurant (데이터는 본문에 JSON으로)
    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(@RequestBody RestaurantDto restaurant) {
        // [설명] @RequestBody: 사용자가 보낸 JSON 데이터(이름, 주소 등)를 RestaurantDto 객체로 변환해서 받습니다.
        // 서비스에게 저장을 시키고, 저장된 결과를 반환합니다.
        return ResponseEntity.ok(restaurantService.save(restaurant));
    }

    // ==========================================
    // 4. 맛집 삭제
    // ==========================================
    // 요청: DELETE /api/v1/restaurant/5 (5번 식당 지워줘)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long id) {
        // [설명] 서비스에게 삭제 명령을 내립니다.
        restaurantService.delete(id);
        // [결과] "맛집 삭제 성공"이라는 메시지를 보냅니다.
        return ResponseEntity.ok("맛집 삭제 성공");
    }

    // ==========================================
    // 5. 블로그 리뷰 검색 (네이버 API 연동)
    // ==========================================
    // 요청: GET /api/v1/restaurant/1/blogs
    @GetMapping("/{id}/blogs")
    public ResponseEntity<RestaurantBlogDto> getRestaurantBlogs(@PathVariable Long id) {
        // [설명]
        // 1. 사용자가 식당 ID를 줍니다.
        // 2. blogService가 그 식당 이름을 가지고 네이버 검색 API를 찔러서 블로그 글을 가져옵니다.
        // 3. 그 결과를 컨트롤러가 받아서 사용자에게 전달합니다.
        RestaurantBlogDto result = blogService.searchBlogList(id);
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // 6. 즐겨찾기(찜하기) 토글
    // ==========================================
    // 요청: POST /api/v1/restaurant/1/favorite
    @PostMapping("/{id}/favorite")
    public ResponseEntity<String> restaurantFavorite(
            @PathVariable Long id, // 어떤 식당을 찜할 건지
            @AuthenticationPrincipal CustomUserDetails userDetails // 🔥 현재 로그인한 사용자 정보 (시큐리티가 넣어줌)
    ) {
        // [로그인 체크] 로그인 안 한 사람이 요청하면 튕겨냅니다.
        if (userDetails == null) {
            // 401 Unauthorized 에러 반환
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 유저 ID를 꺼냅니다.
        String userId = userDetails.getId();

        // [서비스 호출]
        // "RESTAURANTS" 카테고리로, 이 유저가, 이 식당을 찜했다(혹은 취소했다)고 알립니다.
        // (오타 주의: RESTOURANTS -> RESTAURANTS가 맞지만, DB에 맞춰져 있다면 그대로 둡니다)
        favoriteService.toggleFavorite("RESTOURANTS", userId, id);

        return ResponseEntity.ok("즐겨찾기 처리가 완료되었습니다.");
    }

    // ==========================================
    // A. [관리자용] 데이터 동기화 (공공데이터)
    // ==========================================
    // 요청: POST /api/v1/restaurant/sync
    // 기능: 대전시 오픈 API에서 식당 목록을 긁어와서 DB에 저장합니다.
    @PostMapping("/sync")
    public ResponseEntity<String> syncData() {
        // 크롤러 서비스에게 "공공데이터 긁어와!"라고 시킵니다.
        String result = crawlerService.syncRestaurantData();
        // "00건 동기화 완료" 같은 메시지를 반환합니다.
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // B. [관리자용] 이미지 크롤링 시작
    // ==========================================
    // 요청: POST /api/v1/restaurant/images
    // 기능: 식당 이름으로 검색해서 이미지를 수집합니다.
    @PostMapping("/images")
    public ResponseEntity<String> startCrawling() {
        // [비동기 작업 예상] 이미지를 긁어오는 건 시간이 오래 걸리므로,
        // 서비스에게 "시작해"라고 명령만 내리고 바로 응답을 줍니다. (사용자가 기다리지 않게)
        crawlerService.crawlStoreImages();
        return ResponseEntity.ok("이미지 크롤링이 백그라운드에서 시작되었습니다...");
    }
}

//
//상황 1: 사용자가 맛집을 탐색할 때
//
//목록 보기: 사용자가 "맛집" 탭을 누릅니다. -> GET /api/v1/restaurant 호출 -> DB에 있는 맛집 리스트가 쫘르륵 나옵니다.
//
//상세 보기: "성심당"을 클릭합니다. -> GET /api/v1/restaurant/1 호출 -> 주소, 전화번호 등 상세 정보가 뜹니다.
//
//블로그 리뷰: "사람들 후기 좀 볼까?" -> GET /api/v1/restaurant/1/blogs 호출 -> 네이버 블로그 검색 결과가 나옵니다.
//
//        찜하기: "여기 가봐야지!" 하고 하트 버튼을 누릅니다. -> POST /api/v1/restaurant/1/favorite 호출 -> 로그인 확인 후 즐겨찾기에 추가됩니다.
//
//        상황 2: 관리자가 데이터를 채워 넣을 때
//
//데이터 동기화: "대전시청에서 최신 식당 목록 좀 가져와야겠다." -> POST /sync 호출 -> 공공데이터 API를 긁어와서 DB에 저장합니다.
//
//사진 수집: "식당 사진이 없네?" -> POST /images 호출 -> 백그라운드에서 크롤러가 돌아다니며 가게 사진을 수집합니다.