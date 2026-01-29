package com.example.TEAM202507_01.menus.job.controller;

import com.example.TEAM202507_01.menus.job.dto.JobDto; // 화면에 보여줄 데이터 형태
import com.example.TEAM202507_01.menus.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // JSON 데이터를 반환하는 컨트롤러
@RequestMapping("/api/v1/job") // 기본 주소 설정
@RequiredArgsConstructor // final 필드 자동 주입
public class JobController {

    private final JobService jobService;

    // [GET] 크롤링된 공고 조회 API
    // 사용자가 검색어(keyword), 경력(career), 학력(education)을 쿼리 파라미터로 보냄.
    // 예: /crawl?keyword=개발자&career=신입
    @GetMapping("/crawl")
    public ResponseEntity<List<JobDto>> getJobs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "career", required = false) String career,
            @RequestParam(value = "education", required = false) String education
    ) {
        // 서비스에게 검색 조건 3가지를 그대로 넘깁니다.
        // 그리고 서비스가 찾아온 공고 리스트(List<JobDto>)를 그대로 사용자에게 반환합니다.
        return ResponseEntity.ok(jobService.findAllJobPosts(keyword, career, education));
    }
}
//
//사용자가 채용 정보 페이지에 들어가서 "신입", "대졸" 필터를 걸고 검색 버튼을 누릅니다. 이때 프론트엔드는 /api/v1/job/crawl?keyword=자바&career=신입 형태로 요청을 보냅니다. 컨트롤러는 이 요청을 받아서 서비스에게 "자바 신입 공고만 추려서 가져와!"라고 시킵니다.