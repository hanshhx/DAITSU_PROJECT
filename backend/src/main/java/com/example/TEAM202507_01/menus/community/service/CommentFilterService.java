package com.example.TEAM202507_01.menus.community.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CommentFilterService {

    // application.properties에 google.api-key가 있어야 합니다.
    @Value("${google.perspective.api-key}")
    private String apiKey;

    private final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";

    public boolean isToxic(String commentText) {
        //1.API검사
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("⚠️ Google API Key가 설정되지 않았습니다. 필터링을 건너뜁니다.");
            return false; // 키가 없으면 그냥 통과시킴 (기능 마비 방지).
        }
        // 2. 구글 API 호출을 위한 도구 생성
        RestTemplate restTemplate = new RestTemplate();
        // 구글 API 주소에 내 키를 붙여서 요청 주소를 만듦.
        String url = API_URL + apiKey;  // 호출할 주소 완성.

        // 3. 요청 데이터(JSON) 만들기 (Map 사용)
        // [구조 분석] 구글이 요구하는 복잡한 JSON 형식(Map 구조)을 만드는 과정임.
        Map<String, Object> request = new HashMap<>();

        Map<String, String> comment = new HashMap<>();
        comment.put("text", commentText); // 검사할 텍스트 삽입.
        request.put("comment", comment);

        Map<String, Object> requestedAttributes = new HashMap<>();
        requestedAttributes.put("TOXICITY", new HashMap<>());
        request.put("requestedAttributes", requestedAttributes);

        try {
            // RestTemplate이라는 도구로 구글 서버에 편지(POST 요청)를 보냄.
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            // 응답 파싱
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Map<String, Object> attributeScores = (Map<String, Object>) body.get("attributeScores");
                Map<String, Object> toxicity = (Map<String, Object>) attributeScores.get("TOXICITY");
                Map<String, Object> summaryScore = (Map<String, Object>) toxicity.get("summaryScore");

                // 구글의 답장(JSON)을 한 겹 한 겹 까서 점수(score)를 찾아냄.
                // body -> attributeScores -> TOXICITY -> summaryScore -> value
                Double score = (Double) summaryScore.get("value");

                log.info("🤖 댓글 욕설 확률: {} ({})", score, commentText);

                // 0.7 (70%) 이상이면 욕설로 판단하여 true 반환
                return score > 0.7;
            }
        } catch (Exception e) {
            log.error("🔥 필터링 API 호출 중 오류 발생", e);
        }

        // 오류 발생 시 글쓰기를 막지 않기 위해 false 반환 (보수적 접근 시 true)
        return false;
    }
}