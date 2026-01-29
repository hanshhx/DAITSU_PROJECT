package com.example.TEAM202507_01.menus.chatbot.controller; // 1. 패키지 경로

// 2. [Imports] 서비스, 롬복, 스프링 웹 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.chatbot.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController // 3. 이 클래스는 JSON 데이터를 주고받는 API 컨트롤러입니다.
@RequestMapping("/api/v1/chatbot") // 4. "http://서버/api/v1/chatbot"으로 시작하는 주소는 다 이리로 오세요.
@RequiredArgsConstructor // 5. final 변수(geminiService)를 채워주는 생성자 자동 생성
public class ChatBotController {

    // 6. [Service Injection] 실제 AI 통신을 담당하는 서비스를 가져옵니다.
    private final GeminiService geminiService;

    // 7. [POST API] 사용자가 질문을 보낼 때 사용하는 주소 (/chat)
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        // 8. 프론트엔드에서 보낸 JSON 데이터 { "message": "맛집 추천해줘" } 에서 "message" 내용을 꺼냅니다.
        String userMessage = request.get("message");

        // 9. [Service Call] 서비스에게 질문을 넘기고 AI의 답변을 받아옵니다.
        // geminiService 안에 있는 getContents 메서드가 실제 일을 다 합니다.
        String aiResponse = geminiService.getContents(userMessage);

        // 🔥 [추가된 안전장치] AI 응답이 혹시라도 null이면 에러(NullPointerException)가 터집니다.
        // 그래서 null일 경우 기본 메시지를 넣어줍니다.
        if (aiResponse == null) {
            aiResponse = "죄송합니다. 현재 AI 서버 연결이 원활하지 않습니다. 잠시 후 다시 시도해주세요.";
        }

        // 10. [Return] AI의 답변을 다시 JSON { "response": "성심당 추천합니다!" } 형태로 포장해서 돌려줍니다.
        // Map.of()는 값에 null이 들어오면 에러가 나므로, 위에서 안전장치를 거친 aiResponse를 넣습니다.
        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}