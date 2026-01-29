package com.example.TEAM202507_01.menus.hospital.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AiDiagnosisDto {
    private String symptom;          // 사용자가 입력한 증상 ("배가 아파요")
    private String recommendedDept;  // AI가 추천한 과 ("내과")
    private String advice;           // (선택) AI의 간단한 조언 ("소화기 질환일 수 있으니 내과 진료를 권장합니다.")
    private List<HospitalDto> hospitals; // 추천된 과의 병원 목록
}