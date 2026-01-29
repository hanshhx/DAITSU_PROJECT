package com.example.TEAM202507_01.menus.mailgun.service;
// [1] 패키지 선언: 이 파일이 '메일건 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 메일 발송에 필요한 설정값, 도구, 디자인 양식(Form) 등을 가져옵니다.
import com.example.TEAM202507_01.menus.mailgun.Form.mailForm; // 이메일 디자인 양식 클래스
import com.example.TEAM202507_01.menus.mailgun.dto.mailDto; // 메일 데이터 전달용 가방
import io.jsonwebtoken.Jwts; // (현재 코드에선 안 쓰이지만 토큰 생성 시 사용되는 도구)
import jakarta.mail.MessagingException; // 메일 발송 중 발생하는 에러 처리용
import jakarta.mail.internet.MimeMessage; // HTML 메일을 담는 실제 편지 객체
import lombok.RequiredArgsConstructor; // final 필드 생성자 자동 주입
import org.springframework.beans.factory.annotation.Value; // 설정 파일(application.yml)의 값을 읽어옴
import org.springframework.mail.javamail.JavaMailSenderImpl; // 실제 메일을 보내는 핵심 도구
import org.springframework.mail.javamail.MimeMessageHelper; // MimeMessage 작성을 돕는 도우미
import org.springframework.stereotype.Service; // "나 비즈니스 로직 담당 서비스야" 등록
import org.springframework.web.bind.annotation.RequestBody; // (컨트롤러용인데 여기 적힌 건 단순 오타일 가능성이 큼)
import org.springframework.web.bind.annotation.RequestParam; // (마찬가지로 불필요한 임포트이나 동작엔 지장 없음)

@Service
// [3] 어노테이션(@Service): 스프링에게 "내가 메일 발송 로직을 실제로 처리하는 요리사다"라고 알립니다.

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final이 붙은 mailSender를 초기화하는 생성자를 자동으로 만듭니다.
// 스프링이 이 생성자를 통해 '진짜 메일 발송 기계(mailSender)'를 쏙 넣어줍니다(의존성 주입).

public class mailServiceImpl implements mailService {
// [5] implements mailService: "메뉴판(인터페이스)에 있는 약속들을 내가 책임지고 다 지키겠다"는 선언입니다.

    private final JavaMailSenderImpl mailSender;
    // [6] 실제 메일을 발송해주는 스프링의 도구입니다.
    // 우체국의 실제 '우체부' 역할을 한다고 보시면 됩니다.

    // ==========================================
    // 1. 일반 메일 발송 (공통)
    // ==========================================
    @Override // 인터페이스의 메서드를 구현함
    public void sendMail(@RequestBody mailDto mailDto) {
        try {
            // [7] 빈 편지봉투(MimeMessage)를 만듭니다.
            // 단순 텍스트뿐만 아니라 HTML, 첨부파일을 담을 수 있는 큰 봉투입니다.
            MimeMessage message = mailSender.createMimeMessage();

            // [8] 편지 작성을 도와주는 '도우미(Helper)'를 부릅니다.
            // true: 이미지나 파일 등이 들어가는 '멀티파트' 모드 사용
            // "UTF-8": 한글이 깨지지 않도록 하는 설정
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // [9] DTO 가방에서 꺼낸 정보로 봉투 겉면을 채웁니다.
            helper.setTo(mailDto.getTo()); // 수신자 주소
            helper.setSubject(mailDto.getSubject()); // 메일 제목

            // [10] 본문 내용을 채웁니다.
            // 두 번째 파라미터 true: "이건 HTML이니까 디자인 그대로 보여줘"라는 뜻입니다.
            helper.setText(mailDto.getMessage(), true);

            // [11] 우체부(mailSender)에게 봉투를 건네서 발송합니다!
            mailSender.send(message);

            System.out.println("Mail sent successfully");
        } catch (MessagingException e) {
            // 메일 서버 오류나 주소 오타 등으로 전송에 실패하면 여기로 옵니다.
            System.err.println("메일 전송 실패 : " + e.getMessage());
        }
    }

    // ==========================================
    // 2. 아이디 찾기용 인증 메일 발송
    // ==========================================
    @Override
    public void sendFindIdMail(String addr, String token) {
        // [12] mailForm 공장에서 '인증번호용 디자인'에 토큰을 끼워넣어 HTML 텍스트를 가져옵니다.
        String text = mailForm.codeSend(token);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(addr); // 요청한 사용자의 메일 주소
            helper.setSubject("[다잇슈 대전]아이디 찾기"); // 고정 제목
            helper.setText(text, true); // 디자인이 입혀진 HTML 본문

            mailSender.send(message);
            System.out.println("메일 전송완료!");
        } catch (MessagingException e) {
            System.err.println("메일 전송 실패 : " + e.getMessage());
        }
    }

    // [13] 서버의 주소와 포트를 설정 파일에서 가져오거나 수동 설정합니다.
    @Value("${server.address}")
    String domain; // 예: localhost 또는 실제 사이트 도메인
//    String port = "3000"; // 리액트(프론트엔드) 포트 번호

    // ==========================================
    // 3. 비밀번호 재설정 링크 메일 발송
    // ==========================================
    @Override
    public void sendResetPwMail(String addr, String token) {
        if (domain.equals("0.0.0.0")) {
            domain = "192.168.0.134";
        }
        // [14] 사용자가 클릭할 '비밀번호 재설정 페이지'의 전체 주소를 만듭니다.
        // 토큰과 이메일을 파라미터로 붙여서 보안을 유지합니다.
        String link ="http://"+ domain + "/resetPw?token=" + token + "&email=" + addr;

        // [15] mailForm 공장에서 '비밀번호 재설정용(버튼 포함) 디자인'을 가져옵니다.
        String text = mailForm.passwordSend(link);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(addr);
            helper.setSubject("[다잇슈 대전]비밀 번호 재설정");
            helper.setText(text, true);

            mailSender.send(message);
            System.out.println("메일 전송완료!");
        } catch (MessagingException e) {
            System.err.println("메일 전송 실패 : " + e.getMessage());
        }
    }

    // ==========================================
    // 4. 회원가입/가입 인증 이메일 발송
    // ==========================================
    @Override
    public void sendCheckEmail(String email, String token) {
        // [16] 가입 인증용 디자인을 가져옵니다. (인증번호 형태)
        String text = mailForm.codeSend(token);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[다잇슈 대전]이메일 인증");
            helper.setText(text, true);

            mailSender.send(message);
            System.out.println("메일 전송완료!");
        } catch (MessagingException e) {
            System.err.println("메일 전송 실패 : " + e.getMessage());
            // [17] 에러 발생 시 런타임 예외를 던져 상위 계층(Controller 등)에서 알 수 있게 합니다.
            throw new RuntimeException("메일 발송에 실패했습니다. 원인: " + e.getMessage());
        }
    }
}