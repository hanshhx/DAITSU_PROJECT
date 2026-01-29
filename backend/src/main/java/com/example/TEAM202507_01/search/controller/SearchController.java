package com.example.TEAM202507_01.search.controller;

import com.example.TEAM202507_01.search.document.SearchDocument;
import com.example.TEAM202507_01.search.dto.SearchDto;
import com.example.TEAM202507_01.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [어노테이션 분석]
// @RestController: 이 클래스가 뷰(HTML)를 돌려주는 게 아니라, 데이터(JSON)를 반환하는 컨트롤러임을 명시함.
// @RequiredArgsConstructor: final 필드(searchService)에 대한 생성자를 롬복이 자동으로 만들어줌 (의존성 주입).
// @RequestMapping("/api/v1/search"): 이 컨트롤러의 모든 기능은 주소가 '/api/v1/search'로 시작함.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    // [구조 분석]
    // 컨트롤러는 직접 로직을 짜지 않고 서비스에게 일을 시킴.
    // final 키워드를 써서 서비스 객체가 한 번 주입되면 바뀌지 않도록 함.    
    private final SearchService searchService;

    // 1. 통합 검색 기능
    // @GetMapping: HTTP GET 요청을 받음. (주소: /api/v1/search?query=검색어)
    // @RequestParam String query: URL의 ?query=... 부분에서 검색어를 꺼내옴.
    @GetMapping
    public ResponseEntity<SearchDto> search(@RequestParam String query) {
        // searchService.searchIntegrated(query)를 호출하여 검색 결과를 가져오고,
        // ResponseEntity.ok(...)에 담아 200 성공 코드와 함께 반환함.
        return ResponseEntity.ok(searchService.searchIntegrated(query));
    }

    // 2. 식당 데이터 마이그레이션 (DB -> ES)
    // @PostMapping("/restaurantdata"): POST /api/v1/search/restaurantdata 요청을 받음.
    // 보통 데이터를 생성하거나 무거운 작업을 실행할 때 POST를 씀.
    @PostMapping("/restaurantdata")
    public ResponseEntity<String> RestaurantDtoToData(){
        // RDB(MySQL)에 있는 식당 데이터를 ES(엘라스틱서치)로 옮기는 작업을 수행함.
        String result = searchService.restaurantDtoToEs();
        return ResponseEntity.ok(result);
    }

    // 3. 전체 데이터 마이그레이션 (DB -> ES)
    // @PostMapping("/searchdata"): POST /api/v1/search/searchdata 요청을 받음.
    @PostMapping("/searchdata")
    public ResponseEntity<String> allDtoToData() {
        // 식당뿐만 아니라 뉴스, 잡, 투어 등 모든 데이터를 통합 검색 인덱스로 옮기는 작업임.
        String result = searchService.migrateAllData();
        return ResponseEntity.ok(result);
    }
}
