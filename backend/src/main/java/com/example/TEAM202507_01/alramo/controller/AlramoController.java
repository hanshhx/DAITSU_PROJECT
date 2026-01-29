package com.example.TEAM202507_01.alramo.controller; // 이 파일이 위치한 폴더 경로(패키지)입니다.

// 다른 패키지에 있는 서비스 클래스와 필요한 라이브러리들을 가져옵니다.
import com.example.TEAM202507_01.alramo.service.AlramoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: "나는 화면(HTML)을 주는 게 아니라, 데이터(JSON, String 등)를 주는 웨이터(컨트롤러)야"라고 스프링에게 알립니다.
@RestController

// @RequestMapping("api/v1"): "내 담당 구역은 주소창에 'api/v1'이라고 적힌 요청들이야." (기본 경로 설정)
@RequestMapping("api/v1")

// @AllArgsConstructor: "내 안에 있는 final 변수(alramoService)를 채워주는 생성자를 롬복 네가 알아서 만들어줘." (의존성 주입 자동화)
@AllArgsConstructor
public class AlramoController {

    // AlramoService를 사용하기 위해 변수로 선언합니다.
    // private final: "이 변수는 나만 쓸 거고, 처음에 한 번 정해지면 절대 안 바꿔." (안전장치)
    // 위쪽의 @AllArgsConstructor 덕분에 스프링이 알아서 알맞은 서비스를 여기에 꽂아줍니다.
    private final AlramoService alramoService;

    // @GetMapping("/test"): "누가 'GET' 방식으로 '/test' 주소로 들어오면 이 함수를 실행해."
    // 최종 주소: /api/v1/test
    @GetMapping("/test")
    public ResponseEntity<String> test() {

        // [중요] 현재는 주석 처리되어 있어 실행되지 않지만,
        // 주석을 풀면 서비스에게 "새 글 알림 보내!"라고 명령하는 코드입니다.
        // alramoService.sendNewPostNotification();

        // ResponseEntity.ok(...): "모든 게 정상이야(200 OK)."라며 봉투에 "This is a test"라는 편지를 담아 사용자에게 줍니다.
        return ResponseEntity.ok("This is a test");
    }
}