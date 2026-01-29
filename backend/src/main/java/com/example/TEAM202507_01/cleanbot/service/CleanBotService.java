

package com.example.TEAM202507_01.cleanbot.service; // 패키지 경로 선언

// --- [Imports] 필요한 라이브러리들을 가져옵니다. ---
import lombok.Data; // DTO 객체의 Getter, Setter 자동 생성
import lombok.extern.slf4j.Slf4j; // 로그(Log)를 찍기 위한 라이브러리
import org.springframework.beans.factory.annotation.Value; // application.properties의 설정값을 가져옴
import org.springframework.http.HttpEntity; // HTTP 요청 본문과 헤더를 담는 객체
import org.springframework.http.HttpHeaders; // HTTP 헤더 설정 객체
import org.springframework.http.MediaType; // 데이터 타입(JSON) 설정용
import org.springframework.http.ResponseEntity; // HTTP 응답을 받는 객체
import org.springframework.stereotype.Service; // 이 클래스가 서비스(Service)임을 스프링에게 알림
import org.springframework.web.client.RestTemplate; // 외부 API(구글)와 통신하는 도구

import java.util.Collections; // 리스트를 쉽게 만들기 위한 유틸리티
import java.util.HashMap; // 데이터를 키-값(Key-Value) 쌍으로 담는 맵
import java.util.Map;

@Slf4j // 1. 로그 기록을 위한 'log' 변수를 자동으로 만들어줍니다.
@Service // 2. 스프링이 이 클래스를 관리하도록 서비스로 등록합니다.
public class CleanBotService {

    // 3. application.properties 파일에 있는 구글 API 키를 가져와서 apiKey 변수에 넣습니다.
    @Value("${google.perspective.api-key}")
    private String apiKey;

    // 4. application.properties 파일에 있는 구글 API 주소를 가져와서 apiUrl 변수에 넣습니다.
    @Value("${google.perspective.url}")
    private String apiUrl;

    // 5. 외부 서버(구글)에 HTTP 요청을 보낼 도구(RestTemplate)를 생성합니다.
    private final RestTemplate restTemplate = new RestTemplate();

    // 6. 욕설 판단 기준점수입니다. (0.0 ~ 1.0 사이)
    // 0.30(30%) 이상이면 욕설로 간주하겠다는 아주 엄격한 기준입니다.
    private static final double THRESHOLD = 0.30;

    // ==================================================================
    // [Main Method] 텍스트 내용을 검사하는 핵심 함수
    // ==================================================================
    public void checkContent(String text) {
        // 7. [Validation] 검사할 텍스트가 없거나 공백뿐이라면 검사할 필요가 없으니 함수 종료.
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            // 8. https://www.merriam-webster.com/dictionary/build 요청을 보낼 최종 주소를 만듭니다. (URL + ?key=API키)
            String requestUrl = apiUrl + "?key=" + apiKey;

            // 9. [JSON Body Build] 구글에게 보낼 데이터 상자(JSON 구조)를 만듭니다.
            // 구조: { "comment": { "text": "검사할말" }, "languages": ["ko"], ... }
            Map<String, Object> requestBody = new HashMap<>();

            // 9-1. 코멘트 내용 담기
            Map<String, String> comment = new HashMap<>();
            comment.put("text", text); // 실제 검사할 문장 넣기
            requestBody.put("comment", comment); // 전체 본문에 코멘트 추가

            // 9-2. 언어 설정 (한국어 "ko"로 지정) - 리스트 형태로 넣어야 함
            requestBody.put("languages", Collections.singletonList("ko"));

            // 9-3. 요청할 속성 설정 (TOXICITY: 독성/욕설 점수를 달라고 요청)
            Map<String, Object> requestedAttributes = new HashMap<>();
            requestedAttributes.put("TOXICITY", new HashMap<>()); // "욕설 점수 줘!"
            requestBody.put("requestedAttributes", requestedAttributes); // 전체 본문에 속성 요청 추가

            // 10. [Header] "나 JSON 데이터 보낸다"라고 헤더에 명시
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 11. [Packing] 위에서 만든 본문(requestBody)과 헤더(headers)를 하나의 택배 박스(HttpEntity)에 담습니다.
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 12. [Sending] 구글 서버로 POST 요청을 보냅니다! (발송)
            // postForEntity(주소, 보낼데이터, 응답받을클래스타입)
            ResponseEntity<PerspectiveResponse> response = restTemplate.postForEntity(requestUrl, entity, PerspectiveResponse.class);

            // 13. [Checking] 응답이 잘 왔는지 확인합니다.
            if (response.getBody() != null && response.getBody().getAttributeScores() != null) {
                // 14. 응답 구조를 파고 들어가서 '점수(value)'를 꺼냅니다.
                // 구조: response -> attributeScores -> TOXICITY -> summaryScore -> value
                double score = response.getBody()
                        .getAttributeScores()
                        .get("TOXICITY") // 독성 점수 항목 가져오기
                        .getSummaryScore() // 요약 점수 객체 가져오기
                        .getValue(); // 실제 점수(double) 꺼내기

                // 15. 로그에 검사 문장과 점수를 기록합니다. (나중에 확인용)
                log.info("🤖 [CleanBot] 분석 결과: '{}', 점수: {}", text, score);

                // 16. [Judgment] 점수가 기준치(0.30)를 넘었는지 확인합니다.
                if (score > THRESHOLD) {
                    // ★ 17. 기준치를 넘으면 욕설입니다!
                    // 여기서 예외(RuntimeException)를 던져서, 이 함수를 호출한 곳(컨트롤러 등)에서 작업을 중단하게 만듭니다.
                    throw new RuntimeException("🚫 부적절한 표현이 감지되었습니다.");
                }
            }
        } catch (RuntimeException e) {
            // 18. [Exception Handling 1] 우리가 위(17번)에서 일부러 발생시킨 "욕설 감지" 예외인 경우
            if (e.getMessage() != null && e.getMessage().contains("부적절한 표현")) {
                throw e; // 이건 진짜 막아야 하는 상황이니 예외를 그대로 다시 던져서 글 등록을 막습니다.
            }
            // 19. [Fail-Open Strategy] 그 외의 런타임 에러라면? (예: 구글 서버 응답이 이상함)
            // 클린봇 오류 때문에 사용자가 글을 못 쓰는 건 억울하니까, 로그만 남기고 에러를 삼킵니다(통과시킴).
            log.error("⚠️ 클린봇 시스템 오류 (댓글 등록 허용): {}", e.getMessage());
        } catch (Exception e) {
            // 20. [Exception Handling 2] API 키가 틀렸거나, 인터넷이 끊기는 등 예상치 못한 모든 에러
            // 마찬가지로 로그만 남기고, 사용자가 글은 쓸 수 있게 허용해줍니다.
            log.error("⚠️ 클린봇 알 수 없는 오류 (댓글 등록 허용): {}", e.getMessage());
        }
    }

    // ==================================================================
    // [Inner DTO Classes] 구글 API 응답(JSON)을 자바 객체로 받기 위한 그릇들
    // 구글 응답 JSON 구조와 똑같이 만들어야 매핑이 됩니다.
    // ==================================================================

    @Data
    private static class PerspectiveResponse {
        // "attributeScores": { ... } 부분을 받음
        private Map<String, AttributeScore> attributeScores;
    }

    @Data
    private static class AttributeScore {
        // "summaryScore": { ... } 부분을 받음
        private SummaryScore summaryScore;
    }

    @Data
    private static class SummaryScore {
        // "value": 0.85 부분을 받음 (실제 점수)
        private double value;
    }
}
//
//검문 시작 (checkContent):
//
//댓글 등록 서비스가 DB에 저장하기 전에, 이 클린봇에게 "야, 이 문장 괜찮은지 확인해줘"라고 checkContent("바보 멍청이")를 호출합니다.
//
//포장 (Request Build):
//
//클린봇은 구글 API가 알아들을 수 있게 편지(JSON 데이터)를 씁니다.
//
//        "언어는 한국어(ko)이고, 문장 내용은 '바보 멍청이'야. 이게 얼마나 독성(TOXICITY)이 있는지 점수로 알려줘."
//
//발송 및 대기 (API Call):
//
//RestTemplate이라는 우체부를 통해 구글 서버로 편지를 보냅니다.
//
//구글 AI가 분석할 때까지 잠시 기다립니다.
//
//판결 (Score Check):
//
//구글에서 답장(Response)이 옵니다. "이 문장의 독성 점수는 0.85점(85%)입니다."
//
//클린봇은 기준점(THRESHOLD = 0.30)과 비교합니다.
//
//        0.85는 0.30보다 크니까 "삐빅! 욕설입니다!" 하고 에러(RuntimeException)를 터뜨려서 댓글 등록을 강제로 막아버립니다.
//
//예외 상황 (Fail-Safe):
//
//만약 구글 서버가 터졌거나 API 키가 만료되어서 검사를 못 하면?
//
//        catch 블록에서 에러를 잡지만, **"시스템 오류 때문에 사용자 글쓰기를 막으면 안 되지"**라고 판단하여 로그만 남기고 그냥 통과시켜 줍니다. (서비스 안정성 우선)