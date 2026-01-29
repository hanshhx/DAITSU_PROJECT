package com.example.TEAM202507_01.menus.restaurant.service;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다. (셀레니움, 파일 입출력, JSON 파싱, HTTP 통신 등)
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // 맛집 데이터 가방
import com.example.TEAM202507_01.menus.restaurant.repository.RestaurantMapper; // DB 관리자
import com.fasterxml.jackson.annotation.JsonAlias; // JSON 이름 매핑 도구
import com.fasterxml.jackson.databind.DeserializationFeature; // JSON 파싱 설정 도구
import com.fasterxml.jackson.databind.ObjectMapper; // JSON -> 자바 변환기
import io.github.bonigarcia.wdm.WebDriverManager; // 크롬 드라이버 자동 설치기
import lombok.Data; // 롬복 (Getter/Setter)
import lombok.RequiredArgsConstructor; // 생성자 자동 생성
import org.openqa.selenium.By; // HTML 요소 찾기 도구 (id, class 등)
import org.openqa.selenium.WebDriver; // 웹 브라우저 제어 도구
import org.openqa.selenium.WebElement; // HTML 태그 하나를 의미하는 객체
import org.openqa.selenium.chrome.ChromeDriver; // 크롬 브라우저 제어 구현체
import org.openqa.selenium.chrome.ChromeOptions; // 크롬 실행 옵션 (헤드리스 등)
import org.openqa.selenium.support.ui.ExpectedConditions; // "로딩될 때까지 기다려" 조건 설정
import org.openqa.selenium.support.ui.WebDriverWait; // 명시적 대기 도구
import org.springframework.http.HttpEntity; // HTTP 요청 봉투 (헤더 포함)
import org.springframework.http.HttpHeaders; // HTTP 헤더 설정
import org.springframework.http.HttpMethod; // GET, POST 등 방식 설정
import org.springframework.http.ResponseEntity; // HTTP 응답 봉투
import org.springframework.scheduling.annotation.Async; // "이건 백그라운드에서 실행해" (비동기)
import org.springframework.stereotype.Service; // 서비스 빈 등록
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리
import org.springframework.web.client.RestTemplate; // 외부 API 호출 전화기

import java.io.BufferedInputStream; // 파일 다운로드용 입력 스트림
import java.io.File; // 파일/폴더 다루는 도구
import java.io.FileOutputStream; // 파일 저장용 출력 스트림
import java.net.URI; // URL 주소 객체
import java.net.URL; // URL 연결 객체
import java.time.Duration; // 시간 단위 설정 (초, 분 등)
import java.util.ArrayList; // 리스트 도구
import java.util.List; // 리스트 인터페이스

@Service
// [3] 어노테이션(@Service): 스프링에게 "나는 크롤링과 데이터 동기화를 담당하는 일꾼입니다"라고 신고합니다.

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final 변수들을 초기화하는 생성자를 자동으로 만듭니다.
public class RestaurantCrawlerService {

    // [5] 의존성 주입: DB 작업을 위해 매퍼를 데려옵니다.
    private final RestaurantMapper restaurantMapper;

    // [6] 상수(SAVE_PATH): 이미지를 저장할 내 컴퓨터 경로입니다.
    // 주의: 실제 배포할 때는 리눅스 서버 경로("/home/user/images/") 등으로 바꿔야 합니다. 지금은 개발자 PC 경로입니다.
    private final String SAVE_PATH = "C:\\Users\\nextit\\Desktop\\RestaurantImages\\";

    // [7] HTTP 통신 도구: 외부 API(대전시)에 요청을 보낼 때 씁니다.
    private final RestTemplate restTemplate = new RestTemplate();

    // [8] JSON 변환기(ObjectMapper) 설정
    // .configure(...FAIL_ON_UNKNOWN_PROPERTIES, false):
    // "JSON에 내가 모르는 키값이 있어도 에러 내지 말고 쿨하게 무시해라"라는 설정입니다. (안전장치)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // =========================================================
    // [기능 1] 대전시 공공데이터 가져오기 (동기화)
    // =========================================================
    @Transactional // 도중에 에러 나면 저장했던 거 다 취소(Rollback)
    public String syncRestaurantData() {
        System.out.println("========== [동기화 시작] ==========");
        int totalSuccess = 0; // 저장 성공한 개수 세기

        // [9] 헤더 설정 (위장술)
        // 브라우저가 아닌 자바 코드로 요청하면 서버가 "너 로봇이지?" 하고 차단할 수 있습니다.
        // 그래서 "나 윈도우 크롬 쓰는 사람이야"라고 거짓말하는 헤더(User-Agent)를 만듭니다.
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>(headers); // 헤더를 담은 요청 봉투

        // [10] 반복문: 1페이지부터 10페이지까지 데이터를 긁어옵니다.
        for (int i = 1; i <= 10; i++) {
            String url = "https://bigdata.daejeon.go.kr/api/stores/?page=" + i; // 요청 주소
            System.out.println("\n>> " + i + "페이지 요청 중: " + url);

            try {
                // [11] API 호출 (GET 방식)
                // restTemplate이 주소로 가서 데이터를 받아옵니다 (String 형태).
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        URI.create(url), HttpMethod.GET, entity, String.class);

                String jsonString = responseEntity.getBody(); // 응답 본문(JSON 문자열) 꺼내기

                // [12] 응답 검증: 데이터가 비었으면 로그 찍고 다음 페이지로 넘어갑니다.
                if (jsonString == null || jsonString.isEmpty()) {
                    System.out.println("🚨 " + i + "페이지 응답이 비어있음 (NULL/Empty)");
                    continue;
                }
                // 내용 확인용 로그 (앞부분 300자만 출력)
                System.out.println("🔍 응답 내용(앞부분): " + jsonString.substring(0, Math.min(jsonString.length(), 300)));

                // [13] 파싱(Parsing): JSON 문자열 -> 자바 객체(ResponseWrapper) 변환
                // objectMapper가 "이 글자를 보고 객체를 만들어라"라고 일을 합니다.
                ResponseWrapper response = objectMapper.readValue(jsonString, ResponseWrapper.class);

                // [14] 파싱 결과 검증
                if (response == null) {
                    System.out.println("🚨 파싱 실패: response 객체가 NULL");
                    continue;
                }
                if (response.getResults() == null) {
                    System.out.println("🚨 파싱 실패: results 리스트가 NULL (JSON 키 불일치 의심)");
                    continue;
                }
                if (response.getResults().isEmpty()) {
                    System.out.println("⚠️ 파싱 성공했으나 데이터가 0건입니다.");
                    continue;
                }

                System.out.println("✅ 파싱 성공! 데이터 개수: " + response.getResults().size());

                // [15] DB 저장 루프: 리스트에 있는 식당들을 하나씩 꺼내서 저장합니다.
                for (RestaurantDto dto : response.getResults()) {
                    // dto.setCategory("RESTAURANT"); // (필요시 주석 해제하여 카테고리 설정)

                    try {
                        // [16] Null 방지 (안전장치)
                        // 메뉴 리스트 같은 게 null로 오면 나중에 에러 나니까 빈 리스트([])로 바꿔줍니다.
                        if (dto.getMenu() == null) dto.setMenu(new ArrayList<>());
                        if (dto.getPrice() == null) dto.setPrice(new ArrayList<>());
                        if (dto.getMenuDetail() == null) dto.setMenuDetail(new ArrayList<>());

                        // [17] DB에 저장! (매퍼 호출)
                        restaurantMapper.save(dto);
                        totalSuccess++;

                        // 로그 너무 많이 찍히면 정신없으니까 10개마다 점 하나씩 찍어서 진행 상황 표시
                        if (totalSuccess % 10 == 0) System.out.print(".");

                    } catch (Exception e) {
                        // 저장하다 하나 에러 나도 멈추지 말고 에러 로그만 찍고 다음 거 진행
                        System.err.println("\n❌ 저장 에러 (ID: " + dto.getName() + "): " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                // 페이지 전체 요청이 실패했을 때
                System.err.println("\n💥 API 호출 중 에러: " + e.getMessage());
                e.printStackTrace();
            }
        }

        String resultMsg = "\n========== [동기화 종료] 총 " + totalSuccess + "건 저장됨 ==========";
        System.out.println(resultMsg);

        return resultMsg; // 결과 메시지 반환
    }

    // [18] 내부 클래스 (ResponseWrapper)
    // 공공데이터 JSON 구조가 { "count": 100, "next": "...", "results": [...] } 형태라서
    // 이를 받아줄 껍데기 클래스를 만듭니다. static으로 만들어야 독립적으로 쓸 수 있습니다.
    @Data
    public static class ResponseWrapper {
        private int count;
        private String next;

        @JsonAlias("results") // JSON의 "results"라는 키를 이 변수에 매핑하라는 뜻
        private List<RestaurantDto> results;
    }


    // =========================================================
    // [기능 2] 이미지 크롤링 (셀레니움 사용)
    // =========================================================
    @Async
    // [19] @Async: "이 작업은 시간이 오래 걸리니까 백그라운드 스레드에서 따로 돌려라." (사용자는 바로 응답받음)
    public void crawlStoreImages() {
        System.out.println("=== 🕷️ 안전 모드 크롤링 시작 (1건씩 처리) ===");

        // [20] 저장 폴더 만들기
        // 바탕화면에 폴더가 없으면 새로 만듭니다.
        File folder = new File(SAVE_PATH);
        if (!folder.exists()) folder.mkdirs();

        // [21] 크롬 드라이버 설치
        // 내 컴퓨터 크롬 버전에 맞는 드라이버를 알아서 다운받아 설치해줍니다. (편리함)
        WebDriverManager.chromedriver().setup();

        try {
            // [22] 크롤링 대상 조회
            // DB에서 "URL이 있는 식당"만 추려옵니다. (URL 없으면 사진 못 구하니까)
            List<RestaurantDto> storeList = restaurantMapper.findAllWithUrl();

            int count = 0; // 성공 개수 카운트
            for (RestaurantDto store : storeList) {
                // URL 비어있으면 패스
                if (store.getUrl() == null || store.getUrl().isEmpty()) continue;

                // (선택) 이미지가 이미 있는 식당은 건너뛰기 (이어하기 기능)
                // if (store.getImagePath() != null) continue;

                System.out.println("\n>> [" + (count + 1) + "/" + storeList.size() + "] 처리 중: " + store.getName());

                // 🔥 [핵심 전략] 루프 안에서 브라우저를 켰다 끕니다.
                // 한 브라우저로 계속 돌리면 메모리 누수나 세션 꼬임으로 뻗을 수 있어서,
                // "1건 처리 -> 브라우저 종료 -> 다시 시작" 방식으로 안전하게 갑니다.
                WebDriver driver = null;
                try {
                    // [23] 크롬 옵션 설정
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--remote-allow-origins=*"); // 보안 경고 무시
                    options.addArguments("--start-maximized"); // 창 최대화
                    options.addArguments("--disable-popup-blocking"); // 팝업 차단 해제
                    options.addArguments("--headless"); // 🔥 화면 안 띄우고 숨겨서 실행 (속도 빠름, 필수!)
                    // 화면 뜨는 거 보고 싶으면 위 "--headless" 줄을 주석 처리하세요.

                    // 드라이버 실행 (브라우저 열기)
                    driver = new ChromeDriver(options);
                    // 대기 도구 생성 (최대 5초 기다림)
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

                    // 해당 식당 URL로 이동
                    driver.get(store.getUrl());
                    Thread.sleep(1500); // 페이지 로딩될 때까지 1.5초 멍때리기 (매너)

                    // [24] 아이프레임(iframe) 진입
                    // 네이버 지도는 실제 내용이 'entryIframe'이라는 액자 속에 들어있습니다.
                    // 그래서 그 안으로 포커스를 옮겨야(switch) 태그를 찾을 수 있습니다.
                    try {
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("entryIframe"));
                    } catch (Exception e) {
                        // 프레임 없으면 그냥 진행 (구조가 다를 수 있음)
                    }

                    // [25] 이미지 태그 찾기 전략
                    // 전략 1: 메인 영역(div[role='main']) 안의 링크(a) 안의 이미지(img) 찾기
                    List<WebElement> images = driver.findElements(By.cssSelector("div[role='main'] a img"));
                    String targetImgSrc = null;

                    // 전략 1 실패 시 전략 2: 그냥 메인 영역 안의 아무 이미지나 찾기
                    if (images.isEmpty()) {
                        images = driver.findElements(By.cssSelector("div[role='main'] img"));
                    }

                    // [26] 유효한 이미지 고르기
                    for (WebElement img : images) {
                        String src = img.getAttribute("src");
                        // 주소가 있고, http로 시작하며, 이상한 데이터(base64)나 아이콘(.svg)이 아닌 것만 선택
                        if (src != null && src.startsWith("http") && !src.contains("data:image") && !src.contains(".svg")) {
                            targetImgSrc = src;
                            break; // 하나 찾으면 탈출
                        }
                    }

                    // [27] 다운로드 및 DB 업데이트
                    if (targetImgSrc != null) {
                        String fileName = store.getId() + ".jpg"; // 파일명: "식당ID.jpg" (예: 152.jpg)

                        // 다운로드 함수 호출 (아래 정의됨)
                        downloadImage(targetImgSrc, fileName);
                        System.out.println("DEBUG: ID=" + store.getId() + ", FILE=" + fileName);

                        // DB에 파일명 업데이트 ("이제 이 식당 이미지는 152.jpg야")
                        restaurantMapper.updateImage(store.getId(), fileName);
                        System.out.println("   ✅ 저장 완료!");
                        count++;
                    } else {
                        System.out.println("   ⚠️ 이미지 못 찾음");
                    }

                } catch (Exception e) {
                    System.err.println("   💥 " + store.getName() + " 처리 중 에러: " + e.getMessage());
                    // 에러 나도 멈추지 않음
                } finally {
                    // 🔥 [필수] 작업 끝났으면 브라우저 즉시 종료 (메모리 정리)
                    if (driver != null) {
                        try { driver.quit(); } catch (Exception e) {}
                    }
                }

                // [28] 매너 휴식: 너무 빨리 연속 요청하면 네이버가 "공격이다!" 하고 IP 차단합니다. 1초 쉼.
                Thread.sleep(1000);
            }

            System.out.println("=== 🎉 전체 크롤링 종료 (성공: " + count + "건) ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // [29] 보조 메서드: 파일 다운로드
    // 인터넷 이미지 주소(imageUrl)를 받아서 내 컴퓨터(SAVE_PATH)에 저장하는 함수
    private void downloadImage(String imageUrl, String fileName) {
        // try-with-resources 구문: 입출력 스트림을 자동으로 닫아줘서 안전합니다.
        try (BufferedInputStream in = new BufferedInputStream(new URL(imageUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(SAVE_PATH + fileName)) {

            byte[] dataBuffer = new byte[1024]; // 1KB씩 읽기 위한 바구니
            int bytesRead;
            // 데이터를 다 읽을 때까지 반복해서 파일에 씁니다.
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.err.println("   ❌ 다운로드 실패: " + e.getMessage());
        }
    }
}
//
//상황 1: 관리자가 [데이터 동기화] 버튼을 눌렀을 때
//
//준비: 로봇이 대전시 API 서버 주소를 손에 쥡니다.
//
//        위장: 그냥 요청하면 봇이라고 차단당할까 봐, "저 윈도우 쓰는 크롬 사람이에요~"라고 헤더를 조작(User-Agent)합니다.
//
//반복 수집: 1페이지부터 10페이지까지 돌면서 데이터를 요청합니다.
//
//파싱(Parsing): 받아온 JSON 문자열을 ObjectMapper를 써서 자바 객체(RestaurantDto)로 변환합니다.
//
//저장: 변환된 객체를 DB 장부(restaurantMapper)에 기록합니다. 이때 메뉴 리스트가 비어있으면 에러 안 나게 빈 리스트로 초기화해주는 센스도 발휘합니다.
//
//상황 2: 관리자가 [이미지 수집] 버튼을 눌렀을 때
//
//명단 확보: 로봇이 DB에서 "홈페이지 주소(URL)가 있는 식당 명단"을 받아옵니다.
//
//잠입 (Crawling):
//
//명단에 적힌 식당을 하나씩 찾아갑니다.
//
//        중요: 한 번에 다 띄우면 컴퓨터가 뻗으니까, "브라우저 켜기 -> 접속 -> 사진 다운 -> 브라우저 끄기" 이 과정을 한 건 한 건 반복합니다. (안전 제일)
//
//발견: 네이버 지도 페이지 안의 entryIframe이라는 액자(Frame) 속으로 들어가서, <img> 태그를 찾아냅니다.
//
//다운로드: 찾은 이미지 주소(src)를 가지고, 내 컴퓨터 바탕화면 폴더(C:\Users\...)에 123.jpg 이름으로 저장합니다.
//
//        기록: "123번 식당 사진 구했음!" 하고 DB에 파일명을 업데이트합니다.