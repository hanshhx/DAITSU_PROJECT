package com.example.TEAM202507_01.admin.service;

import com.example.TEAM202507_01.admin.dto.AdminDto;
import com.example.TEAM202507_01.admin.repository.AdminMapper;
import com.sun.management.OperatingSystemMXBean; // CPU 정보 가져오는 도구
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service // 비즈니스 로직을 담당하는 서비스임을 명시
@RequiredArgsConstructor // 매퍼 주입용 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 모드로 실행 (성능 최적화)
public class AdminService {
    private final AdminMapper adminMapper;

    // 대시보드 통계 전체를 만들어내는 메서드
    public AdminDto getDashboardStats() {
        AdminDto response = new AdminDto(); // 빈 결과 박스 생성

        // ==========================================
        // 1. 방문자 추이 데이터 가공 (Line Chart용)
        // ==========================================

        // 1-1. 날짜 범위 계산: 오늘부터 6일 전까지 (총 7일)
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        // 1-2. DB에 보낼 파라미터 준비 (yyyy-MM-dd 형식)
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        params.put("endDate", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 1-3. DB 조회: 날짜별 방문자 수 가져오기
        // 주의: 방문자가 0명인 날짜는 DB 결과에 아예 포함되지 않습니다!
        List<Map<String, Object>> dbList = adminMapper.selectDailyVisitCount(params);

        // 1-4. DB 결과를 검색하기 편하게 Map 구조로 변환
        // List<Map> -> Map<날짜문자열, 숫자> 형태로 바꿉니다.
        Map<String, Long> dataMap = new HashMap<>();
        for (Map<String, Object> row : dbList) {
            String key = String.valueOf(row.get("visitDate")); // 날짜 (key)
            Long val = Long.valueOf(String.valueOf(row.get("count"))); // 방문자수 (value)
            dataMap.put(key, val);
        }

        // 1-5. [핵심 로직] 빈 날짜 채우기 (0으로 메꾸기)
        // 오늘부터 6일 전까지 거꾸로 루프를 돌면서 데이터를 채웁니다.
        AdminDto.VisitorTrend trend = new AdminDto.VisitorTrend();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i); // 오늘, 어제, 그저께... 순서
            String dbKey = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 검색용 키 (2026-01-01)
            String label = targetDate.format(DateTimeFormatter.ofPattern("MM-dd"));      // 화면 표시용 (01-01)

            labels.add(label); // 라벨 추가

            // map.getOrDefault(): 해당 날짜의 데이터가 있으면 가져오고, 없으면 0을 가져옵니다.
            // 이렇게 해야 차트가 끊기지 않고 0으로 바닥을 찍으며 그려집니다.
            counts.add(dataMap.getOrDefault(dbKey, 0L));
        }

        // 1-6. 완성된 데이터를 DTO에 담습니다.
        trend.setLabels(labels);
        trend.setThisWeek(counts);
        response.setVisitorTrend(trend);

        // ==========================================
        // 2. 유입 경로 데이터 가공 (Pie Chart용)
        // ==========================================

        // 2-1. DB 조회: 경로별 통계
        List<Map<String, Object>> sourceList = adminMapper.selectTrafficSourceStats();

        // 2-2. 리스트 분리 작업
        AdminDto.TrafficSource sourceStat = new AdminDto.TrafficSource();
        List<String> sourceLabels = new ArrayList<>();
        List<Long> sourceCounts = new ArrayList<>();

        // DB에서 가져온 리스트를 돌면서 라벨과 숫자를 각각의 리스트에 담습니다.
        for (Map<String, Object> map : sourceList) {
            sourceLabels.add(String.valueOf(map.get("source"))); // 예: "Naver"
            sourceCounts.add(Long.valueOf(String.valueOf(map.get("count")))); // 예: 100
        }

        // 2-3. DTO에 담습니다.
        sourceStat.setLabels(sourceLabels);
        sourceStat.setData(sourceCounts);
        response.setTrafficSource(sourceStat);

        // ==========================================
        // 3. 실시간 서버 CPU 사용량 측정
        // ==========================================
        AdminDto.ServerTraffic serverStat = new AdminDto.ServerTraffic();

        try {
            // 3-1. 운영체제 정보를 관리하는 Bean을 가져옵니다.
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            // 3-2. CPU 사용량을 가져옵니다. (0.0 ~ 1.0 사이의 값이라 100을 곱해 퍼센트로 만듭니다)
            double cpuLoad = osBean.getCpuLoad() * 100;

            // 3-3. 가끔 시스템 오류나 초기화 문제로 음수가 나오면 0으로 보정합니다.
            if (cpuLoad < 0) cpuLoad = 0.0;

            // 3-4. DTO에 담습니다. (리스트 형태를 요구하므로 하나만 담아서 리스트로 만듭니다)
            serverStat.setLabels(Collections.singletonList(new Date().toString())); // 현재 시간
            serverStat.setCpu(Collections.singletonList(cpuLoad)); // CPU 사용량

        } catch (Exception e) {
            // 3-5. 윈도우/리눅스 호환성 문제 등으로 에러가 나면 0.0을 반환해서 에러를 방지합니다.
            serverStat.setLabels(Collections.singletonList(new Date().toString()));
            serverStat.setCpu(Collections.singletonList(0.0));
        }

        response.setServerTraffic(serverStat);

        // 4. 최종적으로 꽉 채운 종합 선물 세트(DTO)를 컨트롤러에게 반환합니다.
        return response;
    }
}