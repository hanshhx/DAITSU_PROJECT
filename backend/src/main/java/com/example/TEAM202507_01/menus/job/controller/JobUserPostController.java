package com.example.TEAM202507_01.menus.job.controller;

import com.example.TEAM202507_01.menus.job.dto.JobUserPostDto;
import com.example.TEAM202507_01.menus.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j // 로그 기록용
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/job/user") // 주소 구분 (위의 컨트롤러와 다름)
public class JobUserPostController {

    private final JobService jobService;

    // 1. 인재 목록 조회 (GET /api/v1/job/user/list)
    @GetMapping("/list")
    public ResponseEntity<List<JobUserPostDto>> getUserJobs() {
        log.info("📡 [GET] 인재 목록 조회 요청");
        // 서비스에게 "구직자들 글 다 가져와"라고 시킴
        return ResponseEntity.ok(jobService.findAllJobUserPosts());
    }

    // 2. 인재 프로필 등록 (POST /api/v1/job/user/post)
    @PostMapping("/post")
    public ResponseEntity<?> saveUserJob(@RequestBody JobUserPostDto dto) {
        log.info("📝 [POST] 구직 프로필 등록 요청: {}", dto.getTitle());

        // @RequestBody: 프론트엔드가 보낸 JSON 데이터(제목, 내용 등)를 자바 객체(DTO)로 변환
        // 서비스에게 "이 내용 저장해줘"라고 시킴
        jobService.saveJobUserPost(dto);

        return ResponseEntity.ok("등록 성공");
    }

    // 3. 상세 조회 (GET /api/v1/job/user/{id})
    @GetMapping("/{id}")
    public ResponseEntity<JobUserPostDto> getUserJobDetail(@PathVariable Long id) {
        // URL에 있는 숫자(id)를 읽어서 서비스에게 "이 번호 글 하나만 찾아와"라고 시킴
        return ResponseEntity.ok(jobService.findJobUserPostById(id));
    }
}

//목록 조회: 인사담당자가 "인재 찾기" 메뉴에 들어갑니다. 컨트롤러는 DB에 저장된 구직자들의 이력서를 쫙 뿌려줍니다.
//
//등록: 취준생이 "이력서 등록" 버튼을 누르고 자기소개서를 씁니다. 컨트롤러는 이걸 받아서 저장합니다.