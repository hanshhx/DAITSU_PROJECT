package com.example.TEAM202507_01.menus.news.service;
// [1] 패키지 선언: 이 파일이 '뉴스 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 웹 통신(RestTemplate), HTML 분석(Jsoup), DB 매퍼, 스프링 기능 등을 가져옵니다.
import com.example.TEAM202507_01.menus.news.dto.NewsDto;
import com.example.TEAM202507_01.menus.news.repository.NewsMapper;
import com.example.TEAM202507_01.search.document.SearchDocument; // (현재 코드에선 미사용이나 검색엔진 연동용)
import com.example.TEAM202507_01.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup; // HTML 파싱(크롤링) 도구
import org.jsoup.nodes.Document; // HTML 문서 객체
import org.springframework.beans.factory.annotation.Value; // 설정파일 값 읽기
import org.springframework.context.annotation.PropertySource; // 설정파일 위치 지정
import org.springframework.http.HttpEntity; // HTTP 요청 본문+헤더 포장
import org.springframework.http.HttpHeaders; // HTTP 헤더 설정
import org.springframework.http.HttpMethod; // GET, POST 등 방식 지정
import org.springframework.http.ResponseEntity; // HTTP 응답 처리
import org.springframework.stereotype.Service; // 서비스 빈 등록
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리
import org.springframework.web.client.RestTemplate; // 외부 API 호출 도구
import org.springframework.web.util.UriComponentsBuilder; // URL 생성 도구

import javax.swing.text.html.HTML;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

// [어노테이션 설명]
// @Service: 스프링에게 "이 클래스는 비즈니스 로직을 담당하는 일꾼(Bean)이야"라고 등록표를 붙임.
// @RequiredArgsConstructor: final이 붙은 필드들을 초기화하는 생성자를 자동으로 만들어줌 (의존성 주입).
// @Transactional: 메서드 실행 중 에러가 나면 자동으로 되돌리기(Rollback)를 수행해 데이터 안전을 지킴.
// @PropertySource: "resources 폴더에 있는 application.properties 파일에서 설정을 읽어와라"
@Service
@RequiredArgsConstructor
@Transactional
@PropertySource(value = "classpath:application.properties")
public class NewsServiceImpl implements NewsService {

    // [3] 의존성 주입 (DB 담당)
    // DB에서 뉴스를 조회하거나 삭제할 때 부려먹을 창고지기입니다.
    private final NewsMapper newsMapper;

    // [4] 의존성 주입 (검색엔진 담당)
    // 엘라스틱서치 같은 검색엔진에 데이터를 동기화할 때 씁니다. (현재 코드 로직엔 직접 사용 안 됨)
    private final SearchService searchService;

    // [5] 의존성 주입 (외부 통신 담당)
    // 네이버 서버에 HTTP 요청을 보낼 때 사용하는 '전화기' 같은 도구입니다.
    private final RestTemplate restTemplate;

    // [6] 네이버 API 키 설정
    // application.properties 파일에 적힌 'naver.client.id' 값을 가져와서 변수에 넣습니다.
    @Value("${naver.client.id}")
    private String clientId;

    // application.properties 파일에 적힌 비밀키를 가져옵니다.
    @Value("${naver.client.secret}")
    private String clientSecret;

    // ==========================================
    // 1. 전체 목록 조회 (DB)
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    // [7] 읽기 전용 모드: 조회만 하니까 성능 최적화를 위해 "변경 감지 기능"을 끕니다.
    public List<NewsDto> findAll() {
        // 매퍼에게 "DB에 저장된 뉴스 싹 다 가져와"라고 시킵니다.
        return newsMapper.findAll();
    }

    // ==========================================
    // 2. 상세 조회 (DB)
    // ==========================================
    @Override
    @Transactional(readOnly = true) // 역시 읽기 전용
    public NewsDto findById(Long id) {
        // [8] 매퍼에게 ID로 뉴스 하나를 찾아오라고 합니다.
        NewsDto news = newsMapper.findById(id);

        // [9] 예외 처리: 만약 없는 ID(예: 9999)를 조회하면 null이 오겠죠?
        if (news == null) {
            // 에러를 빵 터뜨려서 컨트롤러에게 "그런 뉴스 없어!"라고 알립니다.
            throw new RuntimeException("해당 뉴스를 찾을 수 없습니다. ID: " + id);
        }
        return news; // 찾은 뉴스 반환.
    }

    // ==========================================
    // 3. 저장/수정 (현재 주석 처리됨)
    // ==========================================
    // [참고] 저장 로직이 필요하면 주석을 풀어서 쓰면 됩니다.
    // @Override
    // public void save(NewsDto news) {
    //    if (news.getId() == null) {
    //        newsMapper.save(news); // ID 없으면 신규 저장
    //    } else {
    //        newsMapper.update(news); // ID 있으면 수정
    //    }
    // }

    // ==========================================
    // 4. 삭제 (DB)
    // ==========================================
    @Override
    public void delete(Long id) {
        // [10] 매퍼에게 "이 ID 가진 뉴스 지워버려"라고 명령합니다.
        newsMapper.delete(id);
    }

    // ==========================================
    // 5. 대전 뉴스 검색 (네이버 API + 크롤링) ★핵심★
    // ==========================================
    // "대전 + 검색어"로 네이버 뉴스를 검색하고, 썸네일까지 긁어옵니다.
    public Object getDaejeonNews(String query, int display, int start) {

        // [11] 요청 URL 만들기 (UriComponentsBuilder)
        // 결과: https://openapi.naver.com/v1/search/news.json?query=대전 맛집&display=8&start=1...
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com") // 네이버 기본 주소
                .path("/v1/search/news.json") // 뉴스 검색 경로
                .queryParam("query", "대전 " + query) // 검색어: "대전"이라는 글자를 강제로 앞에 붙임
                .queryParam("display", display) // 한 번에 가져올 개수 (8개)
                .queryParam("start", start)     // 시작 위치 (페이지 계산된 값)
                .queryParam("sort", "sim")      // 정렬: 정확도순(sim) (날짜순은 'date')
                .encode(StandardCharsets.UTF_8) // 한글 깨짐 방지 인코딩
                .build() // 빌더 완성
                .toUri(); // URI 객체로 변환

        // [12] 헤더 설정 (신분증 제시)
        // 네이버 API는 헤더에 ID와 Secret을 안 넣으면 "누구세요?" 하고 거절합니다.
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        // [13] 요청 엔티티 생성: 헤더를 담은 편지 봉투를 만듭니다. (내용물은 없음)
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // [14] 네이버로 전송! (exchange)
        // uri로, GET 방식으로, 헤더(requestEntity)를 들고 가서, 결과는 NewsDto 클래스 형태로 받아와라.
        ResponseEntity<NewsDto> response = restTemplate.exchange(
                uri, HttpMethod.GET, requestEntity, NewsDto.class
        );

        // [15] 응답 본문 꺼내기
        NewsDto dto = response.getBody();

        // [16] ★ 썸네일 크롤링 로직 ★
        // 네이버는 이미지를 안 줍니다. 그래서 우리가 직접 기사 링크로 가서 이미지를 훔쳐(?)옵니다.
        if (dto != null && dto.getItems() != null) {
            // 받아온 뉴스 기사 리스트를 하나씩 돕니다.
            for (NewsDto.NewsItem item : dto.getItems()) {
                // 아래에 만든 scrapeThumbnail 함수를 호출해 이미지 주소를 따옵니다.
                String thumbnail = scrapeThumbnail(item.getLink());

                // DTO에 썸네일 주소를 채워 넣습니다. (이제 화면에 그림이 뜹니다!)
                item.setThumbnail(thumbnail);
            }
        }

        // 썸네일까지 장착 완료된 완벽한 데이터를 반환합니다.
        return dto;
    }

    // ==========================================
    // [보조 메서드] 썸네일 추출기 (Jsoup)
    // ==========================================
    // 뉴스 기사 URL을 주면, 그 페이지의 대표 이미지(Open Graph Image) 주소를 찾아주는 함수입니다.
    private String scrapeThumbnail(String url) {
        try {
            // [17] Jsoup으로 뉴스 사이트 접속 시도
            Document doc = Jsoup.connect(url)
                    .timeout(2000) // [중요] 2초 안에 응답 안 오면 바로 포기합니다. (안 그러면 전체 목록 로딩이 엄청 느려짐)
                    .userAgent("Mozilla/5.0") // "저 로봇 아니고 사람(브라우저)이에요~" 하고 속임 (차단 방지)
                    .get(); // HTML 문서를 가져옴

            // [18] 메타 태그 찾기
            // HTML 헤더에 있는 <meta property="og:image" content="이미지주소"> 태그를 찾습니다.
            // 보통 카카오톡이나 페이스북에 링크 올릴 때 뜨는 미리보기 이미지입니다.
            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            // 이미지가 비어있으면 null, 있으면 주소 반환
            return imageUrl.isEmpty() ? null : imageUrl;
        } catch (Exception e) {
            // [19] 에러 처리
            // 접속이 안 되거나 이미지가 없어서 에러가 나면, 그냥 쿨하게 null(이미지 없음)을 리턴합니다.
            // 뉴스 하나 이미지 없다고 전체가 멈추면 안 되니까요.
            return null;
        }
    }
}
//
//        네이버 호출 (getDaejeonNews):
//
//        사용자가 검색어를 입력하면, 이 서비스는 **RestTemplate**이라는 전화기를 듭니다.
//
//        네이버 서버에게 전화를 걸어 "대전 축제 관련 뉴스 8개만 JSON으로 주세요!"라고 요청합니다. (이때 ID/비밀번호인 헤더를 보여줍니다.)
//
//        1차 데이터 수신:
//
//        네이버는 뉴스 제목, 요약, 링크를 줍니다. 하지만 이미지(썸네일)는 안 줍니다. 텍스트만 오죠.
//
//        썸네일 채집 (scrapeThumbnail):
//
//        우리 서비스는 포기하지 않습니다. 받아온 뉴스 링크 8개를 하나씩 순회합니다.
//
//**Jsoup**이라는 도구를 써서 각 뉴스 기사 페이지에 몰래 접속(2초 제한)합니다.
//
//        그 페이지 HTML 속에 숨어있는 <meta property="og:image"> 태그(카톡 공유할 때 뜨는 그 이미지)를 찾아냅니다.
//
//        합체 및 반환:
//
//        찾아낸 이미지 주소를 아까 받은 뉴스 데이터에 쏙쏙 끼워 넣습니다.
//
//        이제 텍스트+이미지가 완벽하게 갖춰진 데이터를 컨트롤러에게 전달합니다.