package com.example.TEAM202507_01.menus.news.controller;
// [1] 패키지 선언: 이 파일이 프로젝트의 '뉴스(news) 메뉴 > 컨트롤러(controller)' 폴더에 위치함을 나타냅니다.

// [2] 임포트(Import): 이 클래스에서 사용할 다른 상자(객체)나 도구들을 불러옵니다.
import com.example.TEAM202507_01.menus.news.dto.NewsDto; // 뉴스 정보를 담아 나르는 전용 가방(DTO)
import com.example.TEAM202507_01.menus.news.service.NewsService; // 뉴스 관련 실제 업무를 처리하는 서비스(인터페이스)
import lombok.RequiredArgsConstructor; // final이 붙은 필드(NewsService)를 자동으로 연결해주는 생성자를 만들어주는 도구
import org.springframework.http.ResponseEntity; // 응답 데이터와 함께 HTTP 상태 코드(200 OK 등)를 보내기 위한 포장지
import org.springframework.web.bind.annotation.*; // 웹 요청(GET, POST, DELETE 등)을 처리하기 위한 도구들

import java.util.List; // 데이터를 목록(리스트) 형태로 다루기 위한 자바 기본 도구

// [어노테이션 설명]
// @RestController: "스프링아, 이 클래스는 데이터를 반환하는 REST API용 컨트롤러야." (결과를 JSON 형태로 응답함)
// @RequiredArgsConstructor: "final이 붙은 newsService를 자동으로 사용할 수 있게 준비해줘." (의존성 주입)
// @RequestMapping("/api/v1/news"): "이 컨트롤러는 /api/v1/news로 시작하는 주소는 다 책임질게."
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    // [3] 의존성 주입: 뉴스 실제 로직을 담당하는 서비스를 연결합니다.
    // 인터페이스인 NewsService를 선언하면, 실제 실행 시 NewsServiceImpl이 일을 합니다.
    private final NewsService newsService;

    // ---------------------------------------------------------
    // 1. 뉴스 목록 조회 (GET 방식)
    // 주소: GET /api/v1/news
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<NewsDto>> getNewsList() {
        // [설명]: 서비스에게 "전체 뉴스 목록 다 가져와!"라고 시키고 그 결과를 200 OK 상태와 함께 반환합니다.
        return ResponseEntity.ok(newsService.findAll());
    }

    // ---------------------------------------------------------
    // 2. 뉴스 상세 조회 (GET 방식)
    // 주소: GET /api/v1/news/{id} (예: /api/v1/news/10)
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<NewsDto> getNewsDetail(@PathVariable Long id) {
        // [설명]: @PathVariable은 주소창에 들어온 {id} 값을 변수 'id'로 받겠다는 뜻입니다.
        // 서비스에게 "이 ID 번호에 해당하는 뉴스 하나만 찾아줘"라고 시킵니다.
        return ResponseEntity.ok(newsService.findById(id));
    }

    // [참고]: 등록 및 수정 코드는 현재 주석 처리되어 사용하지 않고 있습니다.
    // @PostMapping
    // public ResponseEntity<NewsDto> createNews(@RequestBody NewsDto news) {
    //     return ResponseEntity.ok(newsService.save(news));
    // }

    // ---------------------------------------------------------
    // 3. 뉴스 삭제 (DELETE 방식)
    // 주소: DELETE /api/v1/news/{id}
    // ---------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNews(@PathVariable Long id) {
        // [설명]: 서비스에게 해당 ID의 뉴스를 삭제하라고 명령합니다.
        newsService.delete(id);
        // [결과]: 삭제가 완료되면 사용자에게 "뉴스 삭제 성공"이라는 문자열 메시지를 보냅니다.
        return ResponseEntity.ok("뉴스 삭제 성공");
    }

    // ---------------------------------------------------------
    // 4. 대전 지역 실시간 뉴스 검색 (GET 방식)
    // 주소: GET /api/v1/news/daejeon?page=1&query=축제
    // ---------------------------------------------------------
    @GetMapping("/daejeon")
    public ResponseEntity<?> getDaejeonNews(
            @RequestParam(defaultValue = "1") int page, // 사용자가 요청한 페이지 번호 (기본값 1)
            @RequestParam(defaultValue = "") String query // 검색어 (기본값 빈칸)
    ) {
        // [로직 1]: 한 번에 화면에 보여줄 뉴스 개수를 8개로 정합니다.
        int display = 12;

        // [로직 2]: 시작 위치(start) 계산 공식
        // 네이버 API 등은 '몇 페이지'가 아니라 '몇 번째 데이터부터'인지(start)를 원합니다.
        // 1페이지면 (1-1)*8 + 1 = 1번부터
        // 2페이지면 (2-1)*8 + 1 = 9번부터 가져오게 계산합니다.
        int start = (page - 1) * display + 1;

        // [로직 3]: 계산된 검색어, 개수(display), 시작위치(start)를 서비스에게 넘겨 실시간 뉴스를 가져옵니다.
        return ResponseEntity.ok(newsService.getDaejeonNews(query, display, start));
    }
}
//
//        목록 보기 (조회): 사용자가 뉴스 탭을 클릭하면 브라우저는 /api/v1/news로 신호를 보냅니다. 컨트롤러는 서비스에게 "DB에 있는 뉴스 리스트 몽땅 가져와"라고 시킵니다.
//
//        상세 내용 보기: 리스트에서 특정 뉴스를 클릭하면 /api/v1/news/5 처럼 ID를 보냅니다. 컨트롤러는 "5번 뉴스 내용만 보여줘"라고 요청을 전달합니다.
//
//        외부 뉴스 검색 (네이버 API 등): 사용자가 "대전" 뉴스를 실시간으로 보고 싶어 하면 /api/v1/news/daejeon 주소를 호출합니다. 이때 컨트롤러는 단순히 데이터를 가져오는 게 아니라, 사용자가 보고 있는 페이지 번호(1페이지, 2페이지 등)를 계산해서 정확한 위치의 뉴스들을 가져오도록 설계되어 있습니다.

//
//        전체 연결 구조 및 요약 (Summary)
//        NewsDto: 뉴스 정보(제목, 내용 등)를 담아 계층 간에 이동시키는 **'데이터 가방'**입니다.
//
//        NewsController: 사용자의 문을 열어주는 **'출입구'**입니다. 요청을 분석해서 서비스에게 배분합니다.
//
//        NewsService (Interface): "우리 뉴스 서비스는 이런 이런 기능이 있다"라고 명시한 **'업무 명세서'**입니다.
//
//        NewsServiceImpl: 서비스 명세서대로 실제 검색 로직을 짜고 DB 매퍼를 호출하는 **'실제 일꾼'**입니다.
//
//        NewsMapper: DB에 접속해서 SQL을 실행해 뉴스를 넣거나 빼는 **'창고 관리인'**입니다.