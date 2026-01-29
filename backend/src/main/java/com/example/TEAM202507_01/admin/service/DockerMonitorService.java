package com.example.TEAM202507_01.admin.service;

import com.example.TEAM202507_01.admin.dto.ContainerStatsDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DockerMonitorService
 * cAdvisor 컨테이너에서 수집된 Docker 모니터링 데이터를 가져와 분석하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerMonitorService {

    // [수정 1] RestTemplate을 직접 new로 생성하지 않고, Config에서 등록된 Bean을 주입받습니다.
    private final RestTemplate restTemplate;
    
    // JSON 파싱을 위한 Jackson 라이브러리 객체입니다.
    private final ObjectMapper objectMapper;

    // cAdvisor API 주소 (Docker Compose 서비스명 'cadvisor' 사용)
    private static final String CADVISOR_URL = "http://cadvisor:8080/api/v1.3/docker/";

    /**
     * 실행 중인 모든 도커 컨테이너의 CPU 사용률 총합을 계산하여 반환합니다.
     * @return 전체 CPU 사용률 (%)
     */
    public double getTotalCpuUsage() {
        try {
            // 1. cAdvisor API 호출하여 원본 JSON 데이터를 문자열로 받아옵니다.
            String response = restTemplate.getForObject(CADVISOR_URL, String.class);

            // 2. [수정 2] 올바른 Jackson 패키지(com.fasterxml...)를 사용하여 JSON을 Map 구조로 변환합니다.
            // Key: 컨테이너 ID, Value: 컨테이너 상세 정보(ContainerInfo)
            Map<String, ContainerStatsDto.ContainerInfo> containers = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, ContainerStatsDto.ContainerInfo>>() {}
            );

            double totalCpuPercent = 0.0;

            // 3. 맵에 담긴 각 컨테이너 정보를 순회합니다.
            for (Map.Entry<String, ContainerStatsDto.ContainerInfo> entry : containers.entrySet()) {
                ContainerStatsDto.ContainerInfo info = entry.getValue();
                
                // 해당 컨테이너의 통계 리스트를 가져옵니다.
                List<ContainerStatsDto.Stat> stats = info.getStats();

                // 4. 통계 데이터가 최소 2개 이상 있어야(이전 값 vs 현재 값) 사용률 계산이 가능합니다.
                if (stats != null && stats.size() >= 2) {
                    // 가장 최근 데이터 (Current)
                    ContainerStatsDto.Stat current = stats.get(stats.size() - 1);
                    // 그 직전 데이터 (Previous)
                    ContainerStatsDto.Stat prev = stats.get(stats.size() - 2);

                    // 두 시점의 차이를 이용해 해당 컨테이너의 CPU 사용률을 계산하고 누적합니다.
                    totalCpuPercent += calculateCpuPercent(current, prev);
                }
            }

            // 5. 소수점 둘째 자리까지 반올림하여 반환합니다.
            return Math.round(totalCpuPercent * 100) / 100.0;

        } catch (Exception e) {
            // 예외 발생 시 로그를 남기고 0.0을 반환하여 시스템 중단을 방지합니다.
            log.error("cAdvisor 데이터 수집 실패: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * 두 시점(Current, Prev)의 CPU 사용량 차이를 시간 차이로 나누어 퍼센트를 계산합니다.
     */
    private double calculateCpuPercent(ContainerStatsDto.Stat current, ContainerStatsDto.Stat prev) {
        // 1. CPU 사용량 누적값의 차이 계산 (나노초 단위)
        long totalUsageDelta = current.getCpu().getUsage().getTotal() - prev.getCpu().getUsage().getTotal();

        // 2. 측정 시각의 차이 계산 (나노초 단위)
        long timeDeltaNs = getNanos(current.getTimestamp()) - getNanos(prev.getTimestamp());

        // 3. 시간 차이가 0 이하이면 계산 불가 (0.0 반환)
        if (timeDeltaNs <= 0) return 0.0;

        // 4. (사용량 증가분 / 시간 경과분) * 100 = 사용률(%) 공식 적용
        // 멀티코어 환경일 경우 100%를 넘을 수 있습니다.
        return ((double) totalUsageDelta / timeDeltaNs) * 100.0;
    }

    /**
     * ISO 8601 형식의 타임스탬프 문자열을 나노초 단위의 Long 값으로 변환합니다.
     */
    private long getNanos(String timestamp) {
        // String("2024-01-01T...") -> Instant 객체로 변환
        Instant instant = Instant.parse(timestamp);
        // (초 * 10억) + 나노초 = 전체 나노초
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }
}