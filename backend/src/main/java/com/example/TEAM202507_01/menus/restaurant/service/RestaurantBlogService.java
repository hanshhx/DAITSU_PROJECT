package com.example.TEAM202507_01.menus.restaurant.service;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 네이버 API 통신, 크롤링(Jsoup), DTO, 매퍼 등을 가져옵니다.
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantBlogDto; // 블로그 검색 결과를 담을 큰 가방
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // 식당 이름 조회용 DTO
import com.example.TEAM202507_01.menus.restaurant.repository.RestaurantMapper; // DB 연결 담당자
import lombok.RequiredArgsConstructor; // 생성자 자동 생성
import org.jsoup.Jsoup; // HTML 파싱(크롤링) 도구
import org.jsoup.nodes.Document; // HTML 문서 객체
import org.jsoup.nodes.Element; // HTML 태그 요소
import org.springframework.beans.factory.annotation.Value; // 설정 파일 값 읽기
import org.springframework.http.*; // HTTP 요청/응답 관련 도구
import org.springframework.stereotype.Service; // 서비스 빈 등록
import org.springframework.web.client.RestTemplate; // 외부 API 호출 도구
import org.springframework.web.util.UriComponentsBuilder; // URL 생성 도구

import javax.swing.text.html.HTML;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
// [3] 어노테이션(@Service): "스프링아, 나는 비즈니스 로직을 처리하는 서비스(Service)야." (Bean 등록)

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final이 붙은 필드들을 자동으로 초기화하는 생성자를 만들어줍니다.
public class RestaurantBlogService {

    // [5] 의존성 주입: DB에서 식당 이름을 찾기 위해 매퍼를 데려옵니다.
    private final RestaurantMapper restaurantMapper;

    // [6] 외부 통신 도구: 네이버 서버에 요청을 보낼 '전화기(RestTemplate)'를 만듭니다.
    // 여기서는 필드에서 바로 new로 생성했지만, 보통은 Bean으로 주입받기도 합니다.
    private final RestTemplate restTemplate = new RestTemplate();

    // [7] 네이버 API 인증 키 (Client ID)
    // application.properties 파일에서 'naver.client.id' 값을 가져옵니다.
    @Value("${naver.client.id}")
    private String clientId;

    // [8] 네이버 API 인증 비밀번호 (Client Secret)
    // application.properties 파일에서 'naver.client.secret' 값을 가져옵니다.
    @Value("${naver.client.secret}")
    private String clientSecret;

    // =========================================================
    // 메인 기능: 블로그 검색 및 썸네일 크롤링
    // =========================================================
    // [수정 1] 리턴 타입을 List -> RestaurantBlogDto로 변경했습니다.
    // 이유: 검색 결과 리스트뿐만 아니라 '전체 개수(total)', '시작점(start)' 정보도 같이 넘겨야 하기 때문입니다.
    public RestaurantBlogDto searchBlogList(Long restaurantId) {

        // 1. DB에서 식당 이름 조회
        // 식당 ID(숫자)만으로는 검색할 수 없으니, DB에서 "성심당" 같은 이름을 찾아옵니다.
        RestaurantDto restaurant = restaurantMapper.findNameById(restaurantId);

        // [방어 로직] 만약 없는 식당 ID라면?
        if (restaurant == null) {
            // null을 리턴하면 에러 나니까, 내용물이 비어있는 빈 객체를 줘서 안전하게 처리합니다.
            return new RestaurantBlogDto();
        }

        // 2. 검색어 만들기
        // 그냥 "성심당"보다 "대전 성심당"이라고 검색해야 더 정확한 맛집 리뷰가 나옵니다.
        // 주소 검색으로 변경
        String query = "대전 " + restaurant.getName();


        // 3. URI(주소) 생성
        // 결과 예시: https://openapi.naver.com/v1/search/blog.json?query=대전 성심당&display=100...
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com") // 기본 주소
                .path("/v1/search/blog.json") // 블로그 검색 경로
                .queryParam("query", query) // 검색어
                .queryParam("display", 100) // 한 번에 100개나 가져오겠다고 설정 (5개는 너무 적으니까요)
                .queryParam("start", 1) // 1번째 글부터
                .queryParam("sort", "sim") // 정확도순(sim) 정렬
                .encode() // 한글 검색어 깨짐 방지 인코딩
                .build() // 빌더 완성
                .toUri(); // URI 객체로 변환

        // 4. 헤더 설정 (신분증 준비)
        // 네이버 API는 헤더에 ID와 Secret이 없으면 요청을 거절합니다.
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        // RequestEntity: 헤더와 URL을 합쳐서 완벽한 '요청 봉투'를 만듭니다.
        RequestEntity<Void> requestEntity = RequestEntity.get(uri).headers(headers).build();

        try {
            // 5. 요청 및 응답 (네이버 호출!)
            // restTemplate.exchange: 네이버 서버에 요청을 보내고, 결과를 RestaurantBlogDto 클래스 형태로 받습니다.
            ResponseEntity<RestaurantBlogDto> response = restTemplate.exchange(requestEntity, RestaurantBlogDto.class);

            // [수정 2] Body 전체(DTO)를 꺼냅니다.
            // 여기에는 total(전체개수), items(블로그 글 리스트) 등이 다 들어있습니다.
            RestaurantBlogDto resultDto = response.getBody();

            // [방어 로직] 응답이 비어있거나, 검색 결과(items)가 하나도 없으면?
            if (resultDto == null || resultDto.getItems() == null) {
                // 빈 객체를 반환하고 끝냅니다.
                return new RestaurantBlogDto();
            }

            // 6. 썸네일 크롤링 및 데이터 정제 (★핵심 기술★)
            // resultDto.getItems(): 블로그 글 100개 리스트
            // .parallelStream(): 100개를 한 줄로(for문) 처리하면 너무 느리니까, 여러 스레드가 동시에 작업하게 합니다. (속도 대폭 향상)
            resultDto.getItems().parallelStream().forEach(item -> {
                try {
                    // (1) 썸네일 구하기: 각 블로그 링크(item.getLink())로 들어가서 이미지를 훔쳐옵니다.
                    String thumb = crawlOgImage(item.getLink());
                    item.setThumbnail(thumb); // 구한 이미지를 DTO에 채워 넣습니다.

                    // (2) HTML 태그 제거: 네이버가 준 제목엔 "<b>성심당</b>" 처럼 태그가 붙어있습니다.
                    // 이걸 cleanHtml() 함수로 깨끗한 글자("성심당")만 남깁니다.
                    item.setTitle(cleanHtml(item.getTitle()));
                    item.setDescription(cleanHtml(item.getDescription()));

                } catch (Exception e) {
                    // [중요] 100개 중에 하나가 에러 나도 멈추지 않습니다.
                    // 그냥 그 글만 이미지 없이 넘어가고 나머지는 계속 진행합니다. (catch 블록이 비어있는 이유)
                }
            });

            // [수정 3] 리스트가 아닌 DTO 전체를 반환!
            // 프론트엔드에서 response.data.total, response.data.items 처럼 접근할 수 있게 통째로 줍니다.
            return resultDto;

        } catch (Exception e) {
            // 통신 중 큰 에러가 나면 콘솔에 내용을 찍고 빈 객체를 반환합니다.
            e.printStackTrace();
            return new RestaurantBlogDto();
        }
    }

    // =========================================================
    // [보조 메서드] 썸네일 크롤러
    // =========================================================
    // 블로그 링크를 주면 대표 이미지 주소(String)를 반환합니다.
    private String crawlOgImage(String blogLink) {
        // 네이버 블로그가 아니거나 링크가 없으면 즉시 종료
        if (blogLink == null || !blogLink.contains("blog.naver.com")) {
            return null;
        }

        // [꿀팁] 모바일 주소 변환
        // PC 버전 네이버 블로그는 HTML 구조가 복잡해서 크롤링이 어렵습니다.
        // "m.blog.naver.com"으로 주소를 바꾸면 HTML이 단순해서 크롤링 성공률이 올라갑니다.
        String mobileUrl = blogLink.replace("https://blog.naver.com", "https://m.blog.naver.com");

        try {
            // Jsoup으로 해당 블로그 페이지 접속 (2초 타임아웃)
            Document doc = Jsoup.connect(mobileUrl)
                    .timeout(2000)
                    .userAgent("Mozilla/5.0") // 봇 아니고 사람인 척 위장
                    .get();

            // <meta property="og:image" content="..."> 태그를 찾습니다.
            // 이게 바로 카톡 공유할 때 뜨는 대표 이미지입니다.
            Element metaOgImage = doc.selectFirst("meta[property=og:image]");

            // 찾았으면 content 속성값(이미지 주소)을 반환
            if (metaOgImage != null) {
                return metaOgImage.attr("content");
            }
        } catch (Exception e) {
            // 접속 안 되거나 이미지 없으면 null 반환
            return null;
        }
        return null;
    }

    // =========================================================
    // [보조 메서드] HTML 태그 청소기
    // =========================================================
    // "<b>안녕</b> &quot;하세요&quot;" 같은 더러운 문자열을 "안녕 '하세요'"로 닦아줍니다.
    private String cleanHtml(String text) {
        if (text == null) return ""; // 글자가 없으면 빈 문자열 반환

        return text.replaceAll("<[^>]*>", "") // 정규식: <로 시작해서 >로 끝나는 모든 태그(<b>, </div> 등)를 삭제
                .replace("&quot;", "\"") // &quot; -> " (따옴표 변환)
                .replace("&amp;", "&")   // &amp; -> & (앤드 기호 변환)
                .replace("&lt;", "<")    // &lt; -> < (부등호 변환)
                .replace("&gt;", ">")    // &gt; -> > (부등호 변환)
                .replace("&nbsp;", " "); // &nbsp; -> 공백 (띄어쓰기 변환)
    }
}
//
//상황: 사용자가 "성심당" 상세 페이지에서 [블로그 리뷰] 탭을 눌렀을 때
//
//이름 확인:
//
//컨트롤러가 식당 ID(번호)를 던져줍니다.
//
//서비스는 먼저 DB 장부를 열어서 이 번호가 "성심당"이라는 걸 확인합니다.
//
//        검색 요청 (Naver API):
//
//서비스가 네이버 서버에게 전화를 겁니다.
//
//"검색어는 '대전 성심당'이고요, 블로그 글 100개만 JSON으로 보내주세요."
//
//        1차 데이터 수신 (Text Only):
//
//네이버가 결과를 줍니다. 제목: "<b>성심당</b> 튀소...", 링크: "blog.naver..."
//
//문제점: 제목에 <b> 같은 태그가 섞여 있고, 결정적으로 썸네일 사진이 없습니다.
//
//대량 작업 (Parallel Stream):
//
//서비스는 "사진이 없으면 안 예쁜데..."라고 생각하며 **크롤링 알바생들을 대거 투입(병렬 처리)**합니다.
//
//알바생들이 동시에 100개의 블로그 링크로 흩어져서 접속합니다.
//
//썸네일 채굴 및 세탁:
//
//각 블로그 페이지 소스(HTML)를 뒤져서 대표 이미지(og:image)를 찾아냅니다.
//
//동시에 제목에 붙은 지저분한 <b> 태그들도 깨끗하게 지웁니다.
//
//최종 납품:
//
//사진과 깨끗한 제목으로 다시 포장된 100개의 리뷰 박스(RestaurantBlogDto)를 컨트롤러에게 전달합니다.