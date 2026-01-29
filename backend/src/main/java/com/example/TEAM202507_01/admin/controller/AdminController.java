package com.example.TEAM202507_01.admin.controller;

// 필요한 클래스들을 임포트합니다.
import com.example.TEAM202507_01.admin.dto.AdminDto;
import com.example.TEAM202507_01.admin.repository.AdminMapper;
import com.example.TEAM202507_01.admin.service.AdminService;
import com.example.TEAM202507_01.admin.service.DockerMonitorService;
import com.example.TEAM202507_01.user.dto.UserAuthDto;
import com.example.TEAM202507_01.user.repository.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j // 로그를 찍기 위한 어노테이션 (디버깅용)
@RestController // 이 클래스가 REST API 요청을 처리하는 컨트롤러임을 명시
@RequestMapping("api/v1/admin") // 이 컨트롤러의 모든 주소는 /api/v1/admin으로 시작함
@RequiredArgsConstructor // final이 붙은 필드들의 생성자를 자동으로 만들어줌 (의존성 주입)
@CrossOrigin(origins = "http://localhost:3000") // 3000번 포트(프론트엔드)에서의 요청을 허용 (CORS 해결)
public class AdminController {

    // 의존성 주입: 서비스와 매퍼들을 가져옵니다.
    private final AdminMapper adminMapper;
    private final AdminService adminService;
    private final UserMapper userMapper;
    private final DockerMonitorService dockerMonitorService;

    // --- [1. 대시보드 통계 조회 API] ---
    // 프론트엔드에서 GET /api/v1/admin/stats 요청을 보내면 실행됩니다.
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        // 1. 기존 서비스에서 방문자 수, 유입 경로 등 DB 데이터를 가져옵니다.
        // (이 시점에서는 serverTraffic 부분이 비어있거나 옛날 값일 수 있습니다)
        AdminDto response = adminService.getDashboardStats();

        // 2. 도커 모니터링 서비스에서 '실시간' CPU 전체 사용량을 가져옵니다.
        double realTimeCpuUsage = dockerMonitorService.getTotalCpuUsage();

        // 3. 가져온 CPU 데이터를 DTO에 주입(Update)합니다.
        // 프론트엔드가 배열 형태(List)를 원하므로 리스트로 감싸서 넣어줍니다.

        // ServerTraffic 객체 생성 (DTO 구조에 따라 new ServerTraffic() 부분은 달라질 수 있음)
        AdminDto.ServerTraffic traffic = new AdminDto.ServerTraffic();

        // 라벨: 현재 시간 (예: "14:30:05")
        String currentTimeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        traffic.setLabels(List.of(currentTimeLabel));

        // 데이터: cAdvisor에서 가져온 CPU 값
        traffic.setCpu(List.of(realTimeCpuUsage));

        // 최종적으로 response 객체에 덮어씌웁니다.
        response.setServerTraffic(traffic);

        // 4. 완성된 데이터를 반환합니다.
        return ResponseEntity.ok(response);
    }

    // --- [2. 방문자 로그 저장 API] ---
    // 사용자가 사이트에 접속할 때마다 프론트엔드가 POST 요청을 보냅니다.
    @PostMapping("/visit")
    public void logVisit(@RequestBody Map<String, String> payload, HttpServletRequest request) {

        // 1. 프론트엔드에서 보낸 JSON 데이터(payload)를 꺼냅니다.
        String currentUrl = payload.get("currentUrl"); // 사용자가 지금 보고 있는 페이지 주소
        String referrer = payload.get("referrer"); // 사용자가 어디서 타고 들어왔는지 (이전 페이지 주소)

        // 2. 요청 객체(request)에서 사용자의 기술적인 정보를 추출합니다.
        String userIp = request.getRemoteAddr(); // 사용자의 IP 주소 (누구냐 넌)
        String userAgent = request.getHeader("User-Agent"); // 사용자의 브라우저 정보 (크롬인지 사파리인지, 모바일인지)

        // 3. 유입 경로 판별 로직: referrer 주소를 보고 어디서 왔는지 판단합니다.
        String source = "Direct"; // 기본값은 직접 접속(주소창 입력)
        if (referrer != null && !referrer.isEmpty()) {
            if (referrer.contains("naver")) source = "Naver"; // 네이버에서 옴
            else if (referrer.contains("google")) source = "Google"; // 구글에서 옴
            else if (referrer.contains("instagram")) source = "Instagram"; // 인스타에서 옴
            else if (!referrer.contains("localhost")) source = "Others"; // 로컬호스트(개발중)가 아니면 기타 사이트
        }

        // 4. 추출한 정보들을 DB에 저장하기 위해 Map에 담습니다.
        Map<String, Object> params = new HashMap<>();
        params.put("trafficSource", source); // 판별된 유입 경로
        params.put("referrerUrl", referrer); // 실제 이전 주소
        params.put("landingPage", currentUrl); // 도착한 페이지
        params.put("userIp", userIp); // IP
        params.put("userAgent", userAgent); // 브라우저 정보

        // 5. DB에 저장합니다. (insert)
        adminMapper.insertVisitLog(params);
    }

    // --- [3. 관리자 여부 확인 API] ---
    // 프론트엔드가 "이 사람 관리자 맞아?"라고 물어볼 때 사용합니다.
    @PostMapping("/isAdmin")
    public ResponseEntity<Boolean> isAdmin(@RequestBody Map<String, String> requestData) {
        // 1. 요청에서 로그인 ID를 꺼냅니다.
        String loginId = requestData.get("loginId");

        // 2. DB에서 해당 아이디를 가진 유저의 권한 정보를 조회합니다.
        List<UserAuthDto> userAuth = userMapper.findUserAuthByLoginId(loginId);

        // 3. 정보가 없으면(없는 유저면) false 반환
        if(userAuth.isEmpty()){
            return ResponseEntity.ok(false);
        }

        // 4. 권한 목록 중에 "ROLE_ADMIN"이 하나라도 있는지 검사합니다.
        // stream().anyMatch(): 하나라도 조건에 맞으면 true를 반환하는 자바의 편리한 기능
        boolean isAdmin = userAuth.stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getRole()));

        // 5. 결과를 반환합니다. (true면 관리자, false면 일반인)
        return ResponseEntity.ok(isAdmin);
    }
}
//
//대시보드 접속: 관리자가 대시보드 페이지에 들어가면, 프론트엔드가 /api/v1/admin/stats로 요청을 보냅니다. 컨트롤러는 이 요청을 받아서 서비스(adminService)에게 "통계 데이터 좀 가져와"라고 시키고, 받은 결과를 다시 프론트엔드에게 줍니다.
//
//방문 기록: 일반 사용자가 사이트에 들어오면, 프론트엔드가 몰래 /api/v1/admin/visit으로 "누군가 들어왔어!"라고 보고합니다. 컨트롤러는 사용자의 IP, 어디서 왔는지(네이버, 구글 등)를 분석해서 DB에 저장하라고 시킵니다.
//
//권한 확인: 페이지에 들어온 사람이 진짜 관리자인지 확인하기 위해 /api/v1/admin/isAdmin 요청을 보냅니다.

