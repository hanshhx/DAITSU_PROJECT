package com.example.TEAM202507_01.admin.dto;

import lombok.Data;
import java.util.List;

@Data // Getter, Setter, toString 등을 자동으로 만들어주는 롬복 어노테이션
public class AdminDto {
    // 대시보드에 필요한 3가지 핵심 데이터를 담는 큰 박스들입니다.
    private VisitorTrend visitorTrend;   // 1. 방문자 추세 (선 그래프용)
    private TrafficSource trafficSource; // 2. 유입 경로 비율 (원 그래프용)
    private ServerTraffic serverTraffic; // 3. 서버 상태 (실시간 게이지용)

    // [내부 클래스 1] 방문자 추세 데이터 구조
    @Data
    public static class VisitorTrend {
        private List<String> labels;   // 가로축: 날짜 (예: ["01-01", "01-02", ...])
        private List<Long> thisWeek;   // 세로축: 방문자 수 (예: [10, 25, ...])
    }

    // [내부 클래스 2] 유입 경로 데이터 구조
    @Data
    public static class TrafficSource {
        private List<String> labels;   // 항목 이름 (예: ["Naver", "Google", ...])
        private List<Long> data;       // 항목별 수치 (예: [50, 30, ...])
    }

    // [내부 클래스 3] 서버 트래픽 데이터 구조
    @Data
    public static class ServerTraffic {
        private List<String> labels;   // 시간 (실시간이라 현재 시간 하나만 들어감)
        private List<Double> cpu;      // CPU 사용량 (퍼센트, 소수점)
    }
}

//컨트롤러가 서비스에게 "데이터 가져와"라고 했을 때, 서비스는 방문자 수(숫자), 유입 경로(글자), 서버 상태(소수점) 등 온갖 잡동사니 데이터를 가져옵니다. 이걸 그냥 주면 엉망이 되니까, AdminDto라는 규격화된 박스에 종류별로 칸막이를 쳐서 예쁘게 담아줍니다.
