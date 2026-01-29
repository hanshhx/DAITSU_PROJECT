package com.example.TEAM202507_01.menus.chatbot.service;

import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto;
import com.example.TEAM202507_01.menus.restaurant.repository.RestaurantMapper;
import com.example.TEAM202507_01.menus.tour.dto.TourDto;
import com.example.TEAM202507_01.menus.tour.repository.TourMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    private final RestTemplate restTemplate;
    private final RestaurantMapper restaurantMapper;
    private final TourMapper tourMapper;

    public String getContents(String prompt) {
        List<RestaurantDto> restaurants = restaurantMapper.findAll();
        List<TourDto> tours = tourMapper.findAll();

        String enhancedPrompt = createEnhancedPrompt(prompt, restaurants, tours);

        return callGeminiApiWithRetry(enhancedPrompt);
    }

    private String createEnhancedPrompt(String userQuestion, List<RestaurantDto> restaurants, List<TourDto> tours) {
        StringBuilder sb = new StringBuilder();

        String dayOfWeek = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        sb.append("당신은 대전 여행 큐레이터 '방방곡곡 AI'입니다. 오늘은 ").append(dayOfWeek).append("입니다.\n");
        sb.append("제공된 DB 내에서만 추천하고, 없는 장소는 절대 지어내지 마세요.\n\n");

        sb.append("[데이터 리스트]\n");
        if (restaurants != null) {
            for (RestaurantDto r : restaurants) {
                sb.append(String.format("- 맛집: %s (ID:%d) / 메뉴:%s / 주소:%s\n", r.getName(), r.getId(), r.getBestMenu(), r.getAddress()));
            }
        }
        if (tours != null) {
            for (TourDto t : tours) {
                sb.append(String.format("- 관광지: %s / 설명:%s / 주소:%s\n", t.getName(), t.getDescription(), t.getAddress()));
            }
        }

        // 🔥 [수정됨] 간격을 줄이고 가독성을 높이는 프롬프트로 변경
        sb.append("\n[‼️ 답변 작성 규칙 - 콤팩트 스타일]\n");
        sb.append("1. **형식**: 장소 추천 시 불필요한 서론을 줄이고, 아래 형식을 엄수하세요.\n");
        sb.append("   ### 장소이름 (지역명)\n");
        sb.append("   핵심 특징을 1~2문장으로 매력적으로 설명.\n");
        sb.append("   [GO:REST:ID:주소] (맛집일 때)\n");
        sb.append("   [GO:TOUR:이름:주소] (관광지일 때)\n\n");

        sb.append("2. **가독성**: 문장 사이의 줄바꿈은 한 번만 하세요. 너무 띄엄띄엄 쓰지 마세요.\n");
        sb.append("3. **강조**: 핵심 키워드는 **굵게** 표시하여 눈에 띄게 하세요.\n");
        sb.append("4. **주소 숨김**: 텍스트 본문에 주소를 절대 적지 마세요. 오직 [GO:...] 코드 안에만 넣으세요.\n");

        sb.append("질문: \"").append(userQuestion).append("\"\n");
        return sb.toString();
    }

    public String analyzeSymptom(String symptom) {
        String prompt = String.format(
                "사용자의 증상: '%s'. \n" +
                        "과목 목록: [내과, 외과, 정형외과, 피부과, 치과, 안과, 이비인후과, 산부인과, 비뇨기과, 신경과, 정신건강의학과] \n" +
                        "설명 없이 진료과목 단어 하나만 답변하세요.",
                symptom
        );
        String result = callGeminiApiWithRetry(prompt);
        return result != null ? result.trim().replace("\n", "") : "내과";
    }

    private String callGeminiApiWithRetry(String promptText) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                return callGeminiApi(promptText);
            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                try { Thread.sleep(2000 * retryCount); } catch (InterruptedException ie) { break; }
            } catch (Exception e) {
                return null;
            }
        }
        return "현재 이용자가 많아 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.";
    }

    private String callGeminiApi(String promptText) {
        if (apiKey == null || apiKey.isEmpty()) return null;

        String url = API_URL + apiKey;
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();

        part.put("text", promptText);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getBody() != null) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> contentRes = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> partsRes = (List<Map<String, Object>>) contentRes.get("parts");
                if (partsRes != null && !partsRes.isEmpty()) return (String) partsRes.get(0).get("text");
            }
        }
        return null;
    }
}