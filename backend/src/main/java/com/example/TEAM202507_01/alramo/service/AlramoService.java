package com.example.TEAM202507_01.alramo.service; // 이 파일의 주소(패키지)

// JSON 데이터를 만들기 위한 도구(Gson)와 Pusher 라이브러리를 가져옵니다.
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import com.pusher.rest.Pusher;

// @Service: "나는 실제 비즈니스 로직(요리)을 담당하는 요리사(서비스)야"라고 스프링에게 명찰을 답니다.
// 스프링은 이 명찰을 보고 메모리에 이 객체를 미리 만들어둡니다(Bean 등록).
@Service
public class AlramoService {

    // Pusher 기능을 사용할 변수(도구)를 선언합니다.
    // final을 붙여서 이 도구가 중간에 다른 걸로 바뀌지 않게 고정합니다.
    private final Pusher pusher;

    // [생성자] 이 서비스가 처음 생성될 때(서버 켜질 때) 딱 한 번 실행됩니다.
    // 여기서 Pusher 설정(로그인)을 미리 다 해놓습니다.
    public AlramoService() {
        // 1. Pusher 객체를 생성하면서 인증 정보를 입력합니다.
        // 순서대로 ("앱 아이디", "공개 키(Key)", "비밀 키(Secret)") 입니다.
        // 이 열쇠들이 맞아야 Pusher 서버가 "어, 너 맞구나. 알림 보내줄게" 하고 허락합니다.
        this.pusher = new Pusher("2092959", "6ed2c483bc85214a1b37", "2f25cf5ea1fc8bf81119");

        // 2. 서버 위치(Cluster)를 설정합니다.
        // "ap3"는 아시아 태평양(도쿄) 지역 서버입니다. 한국에서 제일 빨라서 보통 이걸 씁니다.
        this.pusher.setCluster("ap3");

        // 3. 보안 설정을 켭니다.
        // 통신 내용을 암호화(HTTPS)해서 해커가 중간에 내용을 못 보게 합니다.
        this.pusher.setEncrypted(true);
    }

    // [핵심 기능] 실제로 알림을 보내는 함수입니다.
    // 외부(컨트롤러 등)에서 제목(title)과 주소(url)를 주면 알림을 쏩니다.
    public void sendNewPostNotification(String title, String url) {

        // 1. 데이터를 담을 빈 상자(JSON 객체)를 하나 만듭니다.
        // JSON은 {"이름": "값"} 형태로 데이터를 포장하는 전 세계 공용 포맷입니다.
        JsonObject jsonObject = new JsonObject();

        // 2. 상자에 데이터를 담습니다.
        // "title"이라는 이름표를 붙여서 받아온 제목(title)을 넣습니다.
        jsonObject.addProperty("title", title);

        // "url"이라는 이름표를 붙여서 받아온 주소(url)를 넣습니다.
        jsonObject.addProperty("url", url);

        // 3. Pusher 확성기를 통해 전 세계로 발사(trigger)합니다!
        // - "my-channel": 방송 채널 이름 (프론트엔드는 이 채널을 구독하고 있어야 함)
        // - "new-post": 사건 이름 (프론트엔드는 "새 글" 사건이 터지면 반응하도록 코딩함)
        // - jsonObject: 아까 포장한 실제 내용물 (제목, URL 등)
        pusher.trigger("my-channel", "new-post", jsonObject);
    }
}