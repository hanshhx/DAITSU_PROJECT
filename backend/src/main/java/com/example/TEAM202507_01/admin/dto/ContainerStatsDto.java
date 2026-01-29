package com.example.TEAM202507_01.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class ContainerStatsDto {
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 정의되지 않은 필드는 무시 (에러 방지)
    public static class ContainerInfo {
        private List<Stat> stats;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stat {
        private String timestamp;
        private Cpu cpu;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {
        private CpuUsage usage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CpuUsage {
        // cAdvisor JSON에서 "total"이라는 이름으로 넘어옵니다.
        @JsonProperty("total")
        private Long total;
    }
}
