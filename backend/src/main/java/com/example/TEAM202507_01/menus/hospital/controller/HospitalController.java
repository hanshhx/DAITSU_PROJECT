package com.example.TEAM202507_01.menus.hospital.controller;

import com.example.TEAM202507_01.config.security.CustomUserDetails;
import com.example.TEAM202507_01.menus.hospital.dto.AiDiagnosisDto;
import com.example.TEAM202507_01.menus.hospital.dto.HospitalDto;
import com.example.TEAM202507_01.menus.hospital.dto.HospitalMapDto;
// ⚠️ [중요] GeminiService의 실제 패키지 경로를 확인하세요.
import com.example.TEAM202507_01.menus.chatbot.service.GeminiService;
import com.example.TEAM202507_01.menus.hospital.service.HospitalService;
import com.example.TEAM202507_01.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hospital")
public class HospitalController {

    private final HospitalService hospitalService;
    private final FavoriteService favoriteService;

    // ✅ [수정] GeminiService 주입 (필드 추가)
    private final GeminiService geminiService;

    // 1. 목록 조회 (GET)
    @GetMapping
    public ResponseEntity<List<HospitalDto>> getHospitalList() {
        return ResponseEntity.ok(hospitalService.findAll());
    }

    @GetMapping("/map")
    public ResponseEntity<List<HospitalMapDto>> getHospitalInfo() {
        return ResponseEntity.ok(hospitalService.findInfo());
    }

    // 2. 상세 조회 (GET)
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDto> getHospitalDetail(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.findById(id));
    }

    // 3. 등록 및 수정 (POST)
    @PostMapping
    public ResponseEntity<String> createHospital(@RequestBody HospitalDto hospitalDto) {
        HospitalDto hospital = HospitalDto.builder()
                .id(hospitalDto.getId())
                .category(hospitalDto.getCategory())
                .name(hospitalDto.getName())
                .treatCategory(hospitalDto.getTreatCategory())
                .address(hospitalDto.getAddress())
                .tel(hospitalDto.getTel())
                .build();

        hospitalService.save(hospital);
        return ResponseEntity.ok("병원등록 성공");
    }

    // 4. 삭제 (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHospital(@PathVariable Long id) {
        hospitalService.delete(id);
        return ResponseEntity.ok("병원 삭제 성공");
    }

    // 5. 즐겨찾기 토글
    @PostMapping("/{id}/favorite")
    public ResponseEntity<String> hospitalFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String userId = userDetails.getId();
        favoriteService.toggleFavorite("HOSPITALS", userId, id);

        return ResponseEntity.ok("즐겨찾기 처리가 완료되었습니다.");
    }

    // ==========================================
    // 6. 🔥 [신규] AI 증상 분석 및 병원 추천 (POST /ai-diagnosis)
    // ==========================================
    @PostMapping("/ai-diagnosis")
    public ResponseEntity<AiDiagnosisDto> diagnoseSymptom(@RequestBody Map<String, String> request) {
        String symptom = request.get("symptom"); // 사용자 입력 증상

        // 1. AI에게 물어보기 ("이 증상은 무슨 과야?")
        String department = geminiService.analyzeSymptom(symptom);

        // 2. 해당 진료과 병원 찾기
        List<HospitalDto> allHospitals = hospitalService.findAll();

        // 🔥 [필터링 로직 개선]
        // 단순히 treatCategory만 보는게 아니라, 병원 이름(name)이나 종합병원(category) 여부도 확인합니다.
        List<HospitalDto> recommendedHospitals = allHospitals.stream()
                .filter(h -> {
                    // (1) 진료과목(treatCategory)에 포함되는지? (ex: "소화기내과"에 "내과" 포함)
                    boolean matchTreat = h.getTreatCategory() != null && h.getTreatCategory().contains(department);

                    // (2) 병원 이름(name)에 포함되는지? (ex: "둔산내과"에 "내과" 포함)
                    boolean matchName = h.getName() != null && h.getName().contains(department);

                    // (3) 종합병원인가? (종합병원은 모든 과가 있으므로 추천에 포함)
                    boolean isGeneral = "종합병원".equals(h.getCategory()) || "종합병원".equals(h.getTreatCategory());

                    // 위 조건 중 하나라도 맞으면 결과에 포함
                    return matchTreat || matchName || isGeneral;
                })
                .limit(5) // 결과가 너무 많으면 5개만 추림
                .collect(Collectors.toList());

        // 3. 결과 포장
        AiDiagnosisDto result = AiDiagnosisDto.builder()
                .symptom(symptom)
                .recommendedDept(department)
                .advice(department + " 전문의의 진료를 받아보시는 것을 권장합니다. (종합병원 포함)")
                .hospitals(recommendedHospitals)
                .build();

        return ResponseEntity.ok(result);
    }
}

