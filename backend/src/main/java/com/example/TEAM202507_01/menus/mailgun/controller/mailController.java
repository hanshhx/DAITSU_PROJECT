package com.example.TEAM202507_01.menus.mailgun.controller; // 1. 이 파일이 위치한 폴더 주소입니다. (메일 기능 중 컨트롤러 폴더)

// 2. [Imports] 다른 패키지에 있는 클래스나 기능들을 가져다 쓰기 위해 불러옵니다.
import com.example.TEAM202507_01.alramo.service.AlramoService; // (현재 코드에선 안 쓰이지만 임포트되어 있네요)
import com.example.TEAM202507_01.menus.mailgun.dto.mailDto; // 메일 정보를 담을 데이터 가방(DTO)
import com.example.TEAM202507_01.menus.mailgun.service.mailService; // 실제 메일을 보내는 일을 하는 서비스
import lombok.AllArgsConstructor; // 생성자를 자동으로 만들어주는 롬복 도구
import org.springframework.http.ResponseEntity; // 응답(성공/실패 + 데이터)을 포장하는 객체
import org.springframework.web.bind.annotation.*; // 웹 요청(@GetMapping, @PostMapping 등)을 처리하는 도구들

@RestController
// 3. [핵심] "스프링아, 이 클래스는 웹 요청을 받아서 처리하는 '컨트롤러'야."라고 명찰을 달아줍니다.
// @Controller와 달리, HTML 화면을 보여주는 게 아니라 JSON 같은 '데이터' 그 자체를 응답으로 줍니다.

@RequestMapping("api/v1/mail")
// 4. [주소 설정] "이 컨트롤러 안의 모든 기능은 '도메인/api/v1/mail'로 시작하는 주소로 들어와야 해."
// 예: localhost:8080/api/v1/mail/...

@AllArgsConstructor
// 5. [생성자 자동 생성] 롬복의 기능입니다.
// 이 클래스 안에 있는 모든 필드(여기선 mailService)를 초기화하는 생성자를 자동으로 만들어줍니다.
// 이것 덕분에 스프링이 알아서 mailService 객체를 이 컨트롤러에 '주입(DI)'해줄 수 있습니다.
public class mailController {

    // 6. [의존성 주입] 실제 메일 발송 업무를 처리할 전문가(서비스)를 데려옵니다.
    // final: "이 담당자는 한번 정해지면 바뀌지 않아" (안전장치)
    // 실제로는 스프링이 실행될 때 'mailServiceImpl' 같은 구현체를 여기에 쏙 넣어줍니다.
    private final mailService mailService;


    // 7. [메일 발송 요청 처리 메서드]
    // @PostMapping: "누군가 POST 방식(데이터를 싣고 오는 방식)으로 '/sendmail' 주소를 두드리면 이 메서드를 실행해."
    // 최종 접속 주소: localhost:8080/api/v1/mail/sendmail
    @PostMapping("/sendmail")
    public ResponseEntity<String> sendmail(@RequestBody mailDto mailDto) {
        // 8. [파라미터 설명]
        // ResponseEntity<String>: 결과로 문자열(String)과 상태코드(200, 400 등)를 함께 보낼 거야.
        // @RequestBody: "요청 봉투 안에 들어있는 JSON 데이터(본문)를 꺼내서 mailDto 객체에 알아서 채워 넣어줘."
        // 예: { "to": "user@test.com", "title": "안녕" } -> 자바 객체로 변신!

        try {
            // 9. [업무 지시] 서비스(mailService)에게 일을 시킵니다.
            // "방금 받은 dto 정보(받는사람, 내용 등)대로 메일 좀 보내줘."
            // 만약 메일 보내다가 에러가 나면 여기서 멈추고 catch 블록으로 점프합니다.
            mailService.sendMail(mailDto);

            // 10. [성공 응답] 에러 없이 여기까지 왔다면 성공입니다.
            // ResponseEntity.ok(): 상태 코드 200(OK) 설정
            // "Mail sent successfully": 클라이언트에게 보낼 메시지
            return ResponseEntity.ok("Mail sent successfully");

        } catch (Exception e) {
            // 11. [실패 처리] 메일 보내다가 무슨 이유로든 에러(Exception)가 나면 여기로 옵니다.

            // ResponseEntity.badRequest(): 상태 코드 400(Bad Request) 설정
            // e.getMessage(): 에러가 난 구체적인 이유(예: "이메일 주소 형식이 틀림")를 메시지로 담아서 보냅니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
//
//상황: 사용자가 웹사이트에서 [비밀번호 찾기] 버튼을 눌렀을 때
//
//요청 도착 (Request):
//
//프론트엔드(웹 화면)에서 "user@example.com으로 비밀번호 재설정 메일 보내줘!"라는 내용(JSON 데이터)을 담아 편지 봉투를 보냅니다.
//
//이 봉투는 /api/v1/mail/sendmail이라는 주소로 배달됩니다.
//
//접수 (Controller):
//
//이 클래스(mailController)가 봉투를 받습니다.
//
//        @RequestBody가 봉투를 뜯어서, 그 안에 적힌 내용(받는 사람, 제목, 내용)을 자바가 이해할 수 있는 종이(mailDto)에 옮겨 적습니다.
//
//지시 (Service Call):
//
//컨트롤러는 직접 메일을 보내지 않습니다. 전문 배달부인 mailService에게 "자, 이 내용대로 메일 좀 보내고 와."라고 시킵니다 (mailService.sendMail).
//
//응답 (Response):
//
//성공 시: 배달부가 "잘 보냈습니다!" 하고 오면, 사용자에게 "Mail sent successfully (성공)" 메시지와 함께 200 OK(정상) 도장을 찍어줍니다.
//
//실패 시: 배달부가 "주소가 이상해서 못 보냈어요"라고 하면, 사용자에게 에러 메시지와 함께 400 Bad Request(오류) 도장을 찍어줍니다.