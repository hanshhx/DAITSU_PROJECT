package com.example.TEAM202507_01.menus.job.service;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 서비스(service)' 폴더에 위치한다는 주소입니다.
// 서비스 패키지는 비즈니스 로직의 '목록(Interface)'과 '구현(Class)'이 모여있는 곳입니다.

// [2] 임포트: 이 서비스에서 다룰 '데이터 가방(DTO)'들과 '리스트' 도구를 가져옵니다.
import com.example.TEAM202507_01.menus.job.dto.JobDto; // 👈 [변경됨] 크롤링된 외부 공고 데이터를 담는 가방
import com.example.TEAM202507_01.menus.job.dto.JobUserPostDto; // 우리 사이트 회원이 쓴 구직 글 데이터를 담는 가방
import java.util.List; // 데이터를 여러 개 담아서 나를 때 쓰는 리스트 도구

// [3] 인터페이스 선언: "나는 JobService라는 이름의 메뉴판(계약서)입니다."
// class가 아니라 interface입니다. 즉, "기능 이름만 정의할 테니, 실제 동작은 구현체(Impl)가 알아서 해라"는 뜻입니다.
public interface JobService {

    // =========================================================
    // [Part 1] 크롤링 공고 관련 기능 (외부 데이터: 사람인 등)
    // =========================================================

    // [4] 공고 목록 조회 (검색 기능 포함)
    // 기능: 검색어(keyword), 경력(career), 학력(education)을 주면 조건에 맞는 공고들을 찾아줍니다.
    // 반환값: List<JobDto> (JobPostDto가 아니라 JobDto로 변경됨. 화면에 뿌릴 공고 리스트를 반환)
    List<JobDto> findAllJobPosts(String keyword, String career, String education);

    // [5] 공고 상세 조회
    // 기능: 공고 번호(id)를 딱 하나 주면, 그 공고의 상세 내용을 찾아줍니다.
    // 반환값: JobDto (찾아낸 공고 데이터 하나. 없으면 null이나 에러)
    JobDto findJobPostById(Long id);

    // [6] 공고 저장 (크롤링용)
    // 기능: 크롤링 로봇이 가져온 공고 데이터를 DB에 저장할 때 씁니다.
    // 파라미터: JobDto (저장할 내용이 담긴 상자)
    // 반환값: void (저장만 하고 끝내니까 돌려줄 값 없음)
    void saveJobPost(JobDto dto);


    // =========================================================
    // [Part 2] 사용자 구직 공고 관련 기능 (내부 데이터: 회원 이력서)
    // =========================================================

    // [7] 구직 공고 전체 목록 조회
    // 기능: 우리 사이트 회원들이 올린 "저 일 구해요!" 글들을 전부 가져옵니다.
    // 반환값: List<JobUserPostDto> (회원들의 구직 글 리스트)
    List<JobUserPostDto> findAllJobUserPosts();

    // [8] 구직 공고 상세 조회
    // 기능: 특정 회원의 구직 글 번호(id)를 주면, 그 글의 상세 내용을 보여줍니다.
    // 반환값: JobUserPostDto (찾아낸 구직 글 하나)
    JobUserPostDto findJobUserPostById(Long id);

    // [9] 구직 공고 등록 및 수정
    // 기능: 회원이 쓴 구직 글(또는 수정한 글)을 DB에 저장합니다.
    // 파라미터: JobUserPostDto (제목, 자기소개, 희망연봉 등이 담긴 상자)
    // 반환값: void (저장 완료 후 리턴 없음)
    void saveJobUserPost(JobUserPostDto dto);
}
//
//        두 가지 업무 구분:
//
//        이 서비스는 **"외부 공고(크롤링)"**와 "내부 공고(사용자 구직글)" 두 가지 업무를 동시에 처리합니다.
//
//        컨트롤러의 시선:
//
//        JobController(웹 화면 담당)는 이 파일을 보고 "아, findAllJobPosts를 부르면 검색 결과를 주는구나"라고 알게 됩니다.
//
//        JobUserPostController(구직자 담당)는 "아, saveJobUserPost를 부르면 구직글을 저장해 주는구나"라고 알게 됩니다.
//
//        유연한 연결:
//
//        나중에 "검색 기능을 업그레이드하자!" 해서 구현체(JobServiceImpl)를 뜯어고쳐도, 이 메뉴판(JobService)의 이름만 안 바꾸면 컨트롤러 코드는 수정할 필요가 없습니다. (느슨한 결합)