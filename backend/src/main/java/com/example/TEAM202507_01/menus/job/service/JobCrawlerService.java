package com.example.TEAM202507_01.menus.job.service;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 서비스(service)' 폴더에 있다는 주소입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.entity.JobPost; // DB에 저장할 공고 데이터 객체
import com.example.TEAM202507_01.menus.job.repository.JobMapper; // DB에 SQL을 날려줄 창고지기
import lombok.RequiredArgsConstructor; // final 변수 생성자 자동 생성
import org.jsoup.Jsoup; // 자바에서 HTML을 긁어올 때 쓰는 최고의 도구 (크롤링 핵심)
import org.jsoup.nodes.Document; // HTML 문서 전체를 담는 그릇
import org.jsoup.nodes.Element; // HTML 태그 하나(<div>, <a> 등)를 담는 그릇
import org.jsoup.select.Elements; // 태그 여러 개를 담는 리스트
import org.springframework.stereotype.Service; // "나 비즈니스 로직 담당이야" (Bean 등록)
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리 (에러나면 롤백)

import java.net.URLEncoder; // 한글 주소를 외계어(%EB..)로 바꿔주는 도구
import java.nio.charset.StandardCharsets; // 문자셋(UTF-8) 설정
import java.time.LocalDate; // 날짜 계산용 도구
import java.util.regex.Matcher; // 정규식 매칭 도구 (날짜 숫자 뽑아낼 때 씀)
import java.util.regex.Pattern; // 정규식 패턴 도구

@Service
// [3] 어노테이션(@Service): 스프링에게 "이 클래스는 크롤링 작업을 수행하는 일꾼(Bean)입니다"라고 알림.

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final이 붙은 jobMapper를 초기화하는 생성자를 자동으로 만들어줍니다.
public class JobCrawlerService {

    private final JobMapper jobMapper;
    // [5] DB 저장 도구: 크롤링한 데이터를 DB에 넣으려면 매퍼가 꼭 필요합니다.

    // [6] 상수(SARAMIN_URL): 사람인 검색 기본 주소입니다.
    // 끝에 "searchword=" 뒤에 검색어만 붙이면 바로 검색 결과 페이지가 나옵니다.
    private static final String SARAMIN_URL = "https://www.saramin.co.kr/zf_user/search/recruit?search_area=main&search_done=y&search_optional_item=n&searchType=search&searchword=";

    // [7] 상수(GRADUATION_DATE): 🎓 수료일 (2026년 1월 21일).
    // 이 날짜 이후에 마감되는 공고만 저장하려고 기준을 정해둡니다.
    private static final LocalDate GRADUATION_DATE = LocalDate.of(2026, 1, 21);

    // [8] 상수(TARGET_COUNT): 🎯 목표 수집 개수 (30개).
    // 무한정 긁어오면 시간이 너무 오래 걸리고, 사람인 서버에서 차단당할 수 있으니 적당히 끊습니다.
    private static final int TARGET_COUNT = 30;

    @Transactional
    // [9] 트랜잭션: 크롤링 도중에 에러가 나면, 저장했던 것들도 다 취소(Rollback)해서 깔끔한 상태를 유지합니다.
    public String crawlAndSave() {
        int savedCount = 0; // [10] 현재까지 저장한 공고 개수를 세는 변수
        String keyword = "대전"; // [11] 검색어는 '대전' 지역으로 고정합니다.

        int currentPage = 1; // [12] 1페이지부터 시작합니다.
        int maxPage = 100; // 🚨 [13] 최대 100페이지까지 샅샅이 뒤져보겠다고 설정합니다. (원래 10이었는데 늘림)

        try {
            // [14] URL 인코딩: 한글 "대전"을 인터넷 주소창이 알아듣는 문자(%EB%8C%80%EC%A0%84)로 변환합니다.
            // 이거 안 하면 주소가 깨져서 검색이 안 됩니다.
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // [15] 반복문 시작:
            // 조건 1: 저장된 개수(savedCount)가 목표(30개)보다 작고
            // 조건 2: 현재 페이지(currentPage)가 최대 페이지(100)보다 작을 때까지 계속 돕니다.
            while (savedCount < TARGET_COUNT && currentPage <= maxPage) {

                // [16] 최종 URL 조립: "사람인주소" + "대전" + "&recruitPage=1" 형태로 완성
                String finalUrl = SARAMIN_URL + encodedKeyword + "&recruitPage=" + currentPage;

                // [17] 로그 출력: 콘솔창에 현재 진행 상황을 찍어줍니다. (디버깅용)
                System.out.println("==================================================");
                System.out.println(">>> [Page " + currentPage + "] 탐색 시작... (현재 저장: " + savedCount + "개)");

                // [18] Jsoup 연결 및 문서 가져오기 (핵심!)
                // .connect(): 해당 주소로 접속 시도
                // .userAgent(...): "저 로봇 아니고 윈도우 쓰는 크롬 브라우저 사람이에요"라고 속임 (차단 방지 필수)
                // .timeout(10000): 10초 동안 응답 없으면 에러 내고 포기
                // .get(): HTML 문서 전체를 가져와서 'doc' 변수에 담음
                Document doc = Jsoup.connect(finalUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                // [19] 요소 선택: HTML 중에서 class가 "item_recruit"인 덩어리들을 다 찾습니다.
                // 사람인에서는 공고 하나하나가 이 클래스 이름으로 감싸져 있습니다.
                Elements recruits = doc.select(".item_recruit");

                // [20] 종료 조건: 만약 공고가 하나도 안 잡혔다면?
                // 검색 결과가 끝났거나 페이지 구조가 바뀐 거니까 반복문을 탈출(break)합니다.
                if (recruits.isEmpty()) {
                    System.out.println(">>> 더 이상 검색 결과가 없습니다. 종료합니다.");
                    break;
                }

                int pageSkippedCount = 0; // [21] 날짜 필터 때문에 버려진 공고 개수를 세기 위한 변수

                // [22] 찾은 공고 리스트(recruits)를 하나씩(item) 꺼내서 검사합니다.
                for (Element item : recruits) {

                    // [23] 목표 개수(30개)를 채웠으면 즉시 중단합니다.
                    if (savedCount >= TARGET_COUNT) break;

                    try {
                        // [24] 마감일 텍스트 추출 시도
                        // 먼저 ".job_date .date"라는 위치를 찾아봅니다.
                        String deadlineText = item.select(".job_date .date").text();

                        // [25] 만약 없으면 ".d_day"라는 위치도 찾아봅니다. (사람인 페이지 구조가 다양해서 예비책 마련)
                        if (deadlineText == null || deadlineText.isEmpty()) deadlineText = item.select(".d_day").text();

                        // [26] 그래도 없으면 "상시채용"이라고 간주합니다.
                        if (deadlineText == null) deadlineText = "상시채용";

                        // [27] 1차 관문 (날짜 필터링) ★★★
                        // isAfterGraduation 메서드를 호출해서 수료일 이후인지 검사합니다.
                        // false가 나오면(수료일 전 마감이면) continue로 밑에 코드 무시하고 다음 공고로 넘어갑니다.
                        if (!isAfterGraduation(deadlineText)) {
                            pageSkippedCount++; // 버린 개수 추가
                            continue; // 다음 공고로 점프
                        }

                        // [28] 데이터 추출: HTML 태그 구조를 분석해서 텍스트만 쏙쏙 뽑아냅니다.
                        // .select("...").text(): 해당 태그 안에 있는 글자만 가져옴
                        String title = item.select(".job_tit a").text(); // 제목
                        String company = item.select(".corp_name a").text(); // 회사명
                        // .attr("href"): 링크 주소 속성값만 가져옴. 앞에 "https://www.saramin.co.kr"를 붙여서 완전한 주소로 만듦.
                        String link = "https://www.saramin.co.kr" + item.select(".job_tit a").attr("href");

                        // [29] 상세 조건 추출 (지역, 경력, 학력)
                        // ".job_condition span" 안에 순서대로 [지역, 경력, 학력]이 들어있습니다.
                        Elements conditions = item.select(".job_condition span");
                        // 안전하게 리스트 크기를 확인하고 가져옵니다. (없으면 빈칸이나 "무관" 처리)
                        String location = (conditions.size() >= 1) ? conditions.get(0).text() : "";
                        String career = (conditions.size() >= 2) ? conditions.get(1).text() : "무관";
                        String education = (conditions.size() >= 3) ? conditions.get(2).text() : "무관";

                        // [30] 2차 관문 (중복 체크)
                        // DB에 "이 회사 이름"과 "이 제목"을 가진 공고가 이미 있는지 확인합니다.
                        // 0보다 크면 이미 있다는 뜻이므로 저장 안 하고 넘어갑니다(continue).
                        if (jobMapper.countByCompanyAndTitle(company, title) > 0) {
                            // System.out.println("   (중복) 이미 있음: " + company);
                            continue;
                        }

                        // [31] 객체 생성: 검증을 통과했으니 DB에 넣을 JobPost 객체를 조립(Builder)합니다.
                        JobPost job = JobPost.builder()
                                .category("JOBS") // 카테고리 고정
                                .title(title)
                                .companyName(company)
                                .companyType("무관") // 크롤링으론 알 수 없어서 일단 "무관"
                                .description(location) // 본문 대신 지역 정보를 저장
                                .careerLevel(career)
                                .education(education)
                                .deadline(deadlineText) // "상시채용" or "~ 02/20" 문자열 그대로 저장
                                .link(link)
                                .isActive(1) // 활성 상태 1 (보여줌)
                                .build();

                        // [32] DB 저장: 매퍼에게 시켜서 insert 쿼리 실행!
                        jobMapper.insertJobPost(job);

                        // [33] 카운트 증가: 저장 성공했으니 개수 올림
                        savedCount++;
                        System.out.println("   ✅ [저장] " + company + " (" + deadlineText + ")");

                    } catch (Exception e) {
                        // [34] 예외 처리: 공고 하나 긁다가 에러 나도 멈추지 말고 다음 공고로 넘어가게 합니다.
                        continue;
                    }
                }

                // [35] 페이지 결과 로그: 이번 페이지에서 날짜 때문에 몇 개나 버렸는지 확인합니다.
                System.out.println("   -> 페이지 결과: " + pageSkippedCount + "건 날짜 미달로 제외됨.");

                currentPage++; // [36] 다음 페이지로 이동 (1 -> 2 -> 3...)
                Thread.sleep(1000); // [37] 매너 대기: 1초 정도 쉬어줍니다. (너무 빨리 요청하면 서버가 공격으로 오해해서 차단함)
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 나면 로그 찍음
            return "오류: " + e.getMessage();
        }

        // [38] 최종 결과 반환: 몇 건 저장했는지 메시지를 돌려줍니다.
        return "탐색 종료! 총 " + savedCount + "건 저장됨 (탐색한 페이지: " + (currentPage - 1) + ")";
    }

    // [39] 헬퍼 메서드: 날짜 텍스트를 분석해서 수료일(2026.1.21) 이후인지 판별하는 판사님
    private boolean isAfterGraduation(String text) {
        // 1. "상시"나 "채용시"라는 말이 있으면 -> 기한이 없으니 언제든 지원 가능 -> 합격(true)
        if (text.contains("상시") || text.contains("채용시")) return true;

        // 2. "오늘", "내일"이라는 말이 있으면 -> 수료일 전에 마감됨 -> 탈락(false)
        if (text.contains("오늘") || text.contains("내일")) return false;

        // 3. "~ 05/20" 처럼 물결표(~)가 있는 경우 날짜 분석 시작
        if (text.contains("~")) {
            try {
                // [40] 정규식 패턴: 숫자/숫자 형태(예: 12/31)를 찾습니다.
                Pattern p = Pattern.compile("(\\d{1,2})/(\\d{1,2})");
                Matcher m = p.matcher(text);

                // 패턴을 찾았다면
                if (m.find()) {
                    int month = Integer.parseInt(m.group(1)); // 앞 숫자는 월
                    int day = Integer.parseInt(m.group(2));   // 뒤 숫자는 일

                    // [41] 연도 추측 로직 (꼼수)
                    // 지금이 2025년인데 마감이 '2월'이면 -> 내년(2026년) 2월이겠구나!
                    // 지금이 2025년인데 마감이 '12월'이면 -> 올해(2025년) 12월이겠구나!
                    // 기준: 6월보다 작으면 내년(2026), 아니면 올해(2025)로 계산합니다.
                    int year = (month < 6) ? 2026 : 2025;

                    // [42] 날짜 객체 생성: 위에서 구한 연, 월, 일로 날짜를 만듭니다.
                    LocalDate deadlineDate = LocalDate.of(year, month, day);

                    // [43] 비교: 마감일이 수료일(GRADUATION_DATE)보다 뒤에 있거나 같으면 합격!
                    return deadlineDate.isAfter(GRADUATION_DATE) || deadlineDate.equals(GRADUATION_DATE);
                }
            } catch (Exception e) {
                // 날짜 분석하다 에러 나면, 안전하게 합격 처리해줍니다. (혹시 좋은 공고일 수도 있으니)
                return true;
            }
        }
        // 패턴도 없고 특이한 문자열이면 그냥 합격 처리합니다.
        return true;
    }
}
//
//상황: "수료 후 바로 취업하고 싶어!" 버튼을 눌렀을 때
//
//로봇 출동 (crawlAndSave):
//
//로봇이 "대전"이라는 검색어를 들고 사람인 사이트로 출발합니다.
//
//페이지 탐색 (Browsing):
//
//        1페이지부터 시작해서 100페이지까지 뒤질 각오로 목록을 훑습니다.
//
//        데이터 수집 (Scraping):
//
//화면에 보이는 공고 박스(item_recruit)를 하나 집어듭니다.
//
//마감일, 제목, 회사명, 링크를 메모지에 적습니다.
//
//1차 관문: 날짜 필터링 (isAfterGraduation):
//
//        "잠깐! 이거 마감일 언제야?"
//
//        "오늘 마감? 탈락.", "내일 마감? 탈락."
//
//        "상시 채용? 합격.", "2월 마감? 합격(수료 후 지원 가능하니까)."
//
//이렇게 깐깐하게 날짜를 따져서 지원 가능한 것만 남깁니다.
//
//        2차 관문: 중복 검사:
//
//        "근데 이거 아까 적은 거 아냐?" 하고 DB 장부(jobMapper)를 확인합니다.
//
//이미 있으면 과감히 버리고 다음 공고로 넘어갑니다.
//
//저장 (Saving):
//
//두 관문을 통과한 '알짜배기 공고'만 DB에 최종 저장합니다.
//
//퇴근:
//
//목표한 30개를 다 찾거나, 더 이상 공고가 없으면 로봇은 "수고하셨습니다!" 하고 종료합니다.