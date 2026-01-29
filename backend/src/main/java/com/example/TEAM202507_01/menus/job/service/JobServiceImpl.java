package com.example.TEAM202507_01.menus.job.service;
// [1] 패키지 선언: 이 파일이 '일자리 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 필요한 DTO, Entity, Mapper, 스프링 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.dto.JobDto; // 화면용 크롤링 공고
import com.example.TEAM202507_01.menus.job.dto.JobUserPostDto; // 화면용 사용자 공고
import com.example.TEAM202507_01.menus.job.entity.JobPost; // DB용 크롤링 공고 원본
import com.example.TEAM202507_01.menus.job.entity.JobUserPost; // DB용 사용자 공고 원본
import com.example.TEAM202507_01.menus.job.repository.JobMapper; // 크롤링 DB 관리자
import com.example.TEAM202507_01.menus.job.repository.JobUserPostMapper; // 사용자 DB 관리자
import lombok.RequiredArgsConstructor; // 생성자 자동 생성
import org.springframework.stereotype.Service; // 서비스 빈 등록
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리

import java.util.List; // 리스트 도구
import java.util.stream.Collectors; // 리스트 변환 도구

@Service
// [3] 어노테이션(@Service): "스프링아, 나는 비즈니스 로직을 담당하는 핵심 직원(Bean)이야."

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): "final이 붙은 필드들을 초기화하는 생성자를 자동으로 만들어줘." (의존성 주입)

@Transactional
// [5] 어노테이션(@Transactional): "여기서 하는 모든 작업은 '거래'다. 에러 나면 전부 취소(Rollback)한다." (데이터 안전장치)
public class JobServiceImpl implements JobService {
    // [6] 클래스 선언: JobService 인터페이스(메뉴판)에 적힌 기능들을 실제로 구현하는 클래스입니다.

    private final JobMapper jobMapper;
    // [7] 의존성 주입: 크롤링한 공고 데이터를 DB에서 꺼내올 창고지기 1호입니다.

    private final JobUserPostMapper jobUserPostMapper;
    // [8] 의존성 주입: 사용자가 쓴 공고 데이터를 DB에서 꺼내올 창고지기 2호입니다.


    // =========================================================
    // [Part 1] 크롤링 공고 조회 기능
    // =========================================================

    @Override // "인터페이스에 있는 거 구현함" 표시
    @Transactional(readOnly = true)
    // [9] 읽기 전용 모드: 조회만 하는 기능이라서 "변경 감지" 같은 무거운 기능을 꺼둡니다. (속도 빨라짐)
    public List<JobDto> findAllJobPosts(String keyword, String career, String education) {

        // [10] DB 조회 -> 변환 -> 반환 (파이프라인)
        return jobMapper.findAll(keyword, career, education) // (1) 매퍼에게 조건(키워드 등)을 주고 DB에서 '원본 데이터(Entity)' 리스트를 가져옵니다.

                .stream() // (2) 리스트를 '스트림(Stream)'이라는 컨베이어 벨트에 올립니다. (데이터 가공 준비)

                .map(this::convertToJobDto)
                // (3) 벨트 위를 지나가는 JobPost(원본) 하나하나를 잡아서 'convertToJobDto' 메서드로 보냅니다.
                // 결과: JobPost(원본) -> JobDto(포장된 데이터)로 변신합니다.

                .collect(Collectors.toList());
        // (4) 변신이 끝난 DTO들을 다시 상자에 담아 리스트(List)로 만들어서 반환합니다.
    }

    @Override
    @Transactional(readOnly = true) // 역시 읽기 전용
    public JobDto findJobPostById(Long id) {

        // [11] 상세 조회: 매퍼에게 ID를 주고 공고 하나를 찾아오라고 시킵니다.
        JobPost job = jobMapper.findById(id);

        // [12] 유효성 검사: 만약 없는 ID(예: 9999번)를 요청했다면?
        if (job == null) throw new IllegalArgumentException("공고 없음");
        // 에러를 던져서 "그런 공고 없는데요?" 라고 알려줍니다.

        // [13] 변환 및 반환: 찾은 원본 데이터를 DTO로 변환해서 줍니다.
        return convertToJobDto(job);
    }

    @Override
    public void saveJobPost(JobDto dto) {
        // [14] 빈 메서드: 현재 이 프로젝트 구조상 크롤링 저장은 'JobCrawlerService'가 따로 담당하고 있어서
        // 여기는 인터페이스 규격 맞추기용으로 비워두거나, 나중에 관리자용 수동 등록 기능을 위해 남겨둡니다.
        /* 기존 유지 */
    }


    // =========================================================
    // [Part 2] 사용자 구직 공고 (JobUserPost) 기능
    // =========================================================

    @Override
    @Transactional(readOnly = true) // 읽기 전용
    public List<JobUserPostDto> findAllJobUserPosts() {
        // [15] 사용자 공고 전체 목록 조회
        // 매퍼에서 가져온 Entity 리스트를 -> Stream으로 돌려서 -> DTO로 변환하고 -> 다시 리스트로 만듭니다.
        return jobUserPostMapper.findAll().stream()
                .map(this::convertToJobUserPostDto) // DTO 변환 (아래에 정의된 메서드 사용)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용
    public JobUserPostDto findJobUserPostById(Long id) {
        // [16] 사용자 공고 상세 조회
        JobUserPost post = jobUserPostMapper.findById(id);

        // [17] 없으면 에러 발생
        if (post == null) throw new IllegalArgumentException("게시물 없음");

        // [18] 있으면 DTO로 변환해서 반환
        return convertToJobUserPostDto(post);
    }

    @Override
    // [중요] 저장과 수정을 동시에 처리하는 메서드
    public void saveJobUserPost(JobUserPostDto dto) {

        // [19] 방어 로직 (Safe Deadline)
        // 사용자가 날짜를 안 보냈으면(null) 그냥 null로 저장하고, 보냈으면 그 값을 씁니다.
        // 삼항 연산자: (조건) ? 참일때값 : 거짓일때값
        String safeDeadline = (dto.getDeadline() != null && !dto.getDeadline().isEmpty())
                ? dto.getDeadline() : null;

        // [20] DTO -> Entity 변환 (빌더 패턴)
        // 화면에서 받은 데이터(Dto)를 DB에 넣을 데이터(Entity)로 옮겨 담습니다.
        JobUserPost post = JobUserPost.builder()
                .id(dto.getId())                // 글 번호 (수정일 땐 있고, 새 글일 땐 null임)
                .category("JOBS")               // 카테고리 고정
                .userId(dto.getUserId())        // 작성자 ID
                .title(dto.getTitle())          // 제목
                .companyName(dto.getCompanyName()) // 회사명 (보통 비워둠)
                .companyType(dto.getCompanyType()) // 회사 타입
                .description(dto.getDescription()) // 본문 내용
                .careerLevel(dto.getCareerLevel()) // 경력
                .education(dto.getEducation())     // 학력
                .deadline(safeDeadline)            // 위에서 만든 안전한 날짜값
                .isActive(dto.getIsActive())       // 공개 여부
                .build();

        // [21] 저장(Insert) vs 수정(Update) 판별 로직
        // ID가 없으면(null) -> "아, 새로 쓰는 글이구나" -> INSERT 실행
        if (post.getId() == null) {
            jobUserPostMapper.insertJobUserPost(post);
        } else {
            // ID가 있으면(숫자) -> "아, 기존 글 고치는구나" -> UPDATE 실행
            jobUserPostMapper.updateJobUserPost(post);
        }
    }

    // =========================================================
    // [Part 3] 변환 로직 (Helper Methods)
    // =========================================================

    // [22] 크롤링 데이터 변환기 (Entity -> DTO)
    // DB 원본 데이터를 화면용 데이터로 바꾸는 내부 메서드입니다.
    private JobDto convertToJobDto(JobPost job) {

        // [23] 안전한 링크 처리
        // 만약 크롤링된 데이터에 링크가 없으면(null), 클릭 시 에러 안 나게 사람인 메인 주소를 넣어줍니다.
        String safeLink = (job.getLink() == null || job.getLink().isEmpty()) ? "https://www.saramin.co.kr" : job.getLink();

        // [24] 빌더로 DTO 생성
        return JobDto.builder()
                .id(job.getId())
                .category(job.getCategory())
                .title(job.getTitle())
                .companyName(job.getCompanyName())
                .companyType(job.getCompanyType())
                .description(job.getDescription())
                .careerLevel(job.getCareerLevel())
                .education(job.getEducation())
                .deadline(job.getDeadline())
                .link(safeLink) // 여기서 처리한 안전한 링크를 넣습니다.
                .isActive(job.getIsActive())
                .build();
    }

    // [25] 사용자 공고 변환기 (Entity -> DTO)
    // 사용자 글 원본을 화면용 DTO로 바꿉니다.
    private JobUserPostDto convertToJobUserPostDto(JobUserPost post) {
        return JobUserPostDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .userId(post.getUserId())
                .title(post.getTitle())
                .companyName(post.getCompanyName())
                .companyType(post.getCompanyType())
                .description(post.getDescription())
                .careerLevel(post.getCareerLevel())
                .education(post.getEducation())
                .deadline(post.getDeadline()) // 날짜는 문자열 그대로 전달
                .isActive(post.getIsActive())
                .build();
    }
}
//
//상황 1: 구직자가 "자바 신입" 공고를 검색할 때
//
//주문: 컨트롤러가 findAllJobPosts("자바", "신입", null)을 호출합니다.
//
//재료 확보: jobMapper에게 시켜서 DB에서 조건에 맞는 공고들을 몽땅 가져옵니다.
//
//손질: 가져온 데이터(JobPost)는 날것이라 바로 내보내기 좀 그렇습니다. (링크가 비어있을 수도 있음). 그래서 convertToJobDto 메서드로 하나하나 예쁘게 다듬습니다.
//
//서빙: 다듬어진 목록을 리스트에 담아 반환합니다.
//
//        상황 2: 회원이 "저 취업했어요" 하고 글을 수정할 때
//
//주문: 컨트롤러가 수정된 정보가 담긴 dto를 주며 saveJobUserPost를 호출합니다.
//
//판단: DTO 안에 id가 들어있는지 봅니다. "어? ID가 있네? 이건 수정(Update)이구나."
//
//변환: 화면용 데이터(DTO)를 DB 저장용 데이터(Entity)로 옮겨 담습니다.
//
//        지시: jobUserPostMapper.update...를 호출해 DB 내용을 바꿉니다.