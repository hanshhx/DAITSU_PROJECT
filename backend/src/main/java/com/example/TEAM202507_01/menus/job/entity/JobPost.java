package com.example.TEAM202507_01.menus.job.entity;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 엔티티(entity)' 폴더에 위치한다는 주소입니다.
// Entity 패키지는 DB 테이블과 직접 연결되는 '원본 데이터' 클래스들을 모아두는 곳입니다.

// [2] 임포트: 롬복(Lombok)과 날짜 관련 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.repository.JobMapper;
import com.example.TEAM202507_01.menus.job.service.JobCrawlerService;
import lombok.*; // @Getter, @Setter, @Builder 등을 쓰기 위한 마법 도구 세트
import org.apache.ibatis.annotations.Insert;

import java.time.LocalDateTime; // '년-월-일 시:분:초'를 다루는 최신 자바 날짜 도구 (생성일시용)
import java.util.Date;          // 옛날 날짜 도구 (여기선 임포트만 되고 실제론 안 쓰임, 호환성 위해 둠)

// [3] 어노테이션(@Getter, @Setter): 필수 기능 자동 생성
// 의미: "이 클래스 안의 모든 변수에 대해 값 꺼내기(Get)와 값 넣기(Set) 코드를 자동으로 만들어줘."
// 효과: 코드가 수십 줄 줄어들고 깔끔해집니다.
@Getter @Setter

// [4] 어노테이션(@NoArgsConstructor): 빈 객체 생성기
// 의미: "파라미터 없는 기본 생성자(new JobPost())를 만들어줘."
// 중요: DB 라이브러리(MyBatis)가 데이터를 읽어와서 객체를 만들 때, 일단 빈 깡통부터 만들고 데이터를 채우기 때문에 필수입니다.
@NoArgsConstructor

// [5] 어노테이션(@AllArgsConstructor): 꽉 찬 객체 생성기
// 의미: "모든 필드 값을 한 번에 채우는 생성자를 만들어줘."
// 중요: 아래 @Builder 기능을 쓰려면 전체 생성자가 꼭 필요해서 짝꿍으로 씁니다.
@AllArgsConstructor

// [6] 어노테이션(@Builder): 객체 조립 도우미
// 의미: "JobPost.builder().title('공고').build() 처럼 이름표 붙여서 객체를 만들게 해줘."
// 효과: 크롤링한 데이터를 넣을 때 필드가 많아서 순서가 헷갈리는데, 빌더를 쓰면 실수할 일이 없습니다.
@Builder

// [7] 클래스 선언: "지금부터 크롤링한 공고 데이터를 담을 JobPost 클래스를 정의할게."
public class JobPost {

    // [참고] 필드는 DTO와 거의 같습니다.
    // 차이점: DTO는 화면(프론트엔드) 입맛에 맞춘 거고, Entity는 DB 테이블 입맛에 맞춘 겁니다.

    // [8] 필드(id): 기본 키(Primary Key)
    // 의미: 이 공고의 고유 등록 번호입니다.
    // 용도: 데이터 관리의 핵심 키입니다. DB에서 자동으로 1, 2, 3... 번호를 붙여줍니다.
    private Long id; // 글 번호.

    // [9] 필드(category): 게시판 분류
    // 의미: 이 글이 어떤 게시판용인지 표시합니다. 여기선 무조건 'JOBS'가 들어갑니다.
    private String category; // 'JOBS' (채용정보 카테고리).

    // [10] 필드(title): 공고 제목
    // 의미: 크롤링해온 공고의 제목입니다. (예: "2026년 카카오 신입 개발자 공채")
    private String title; // 공고 제목.

    // [11] 필드(companyName): 회사 이름
    // 의미: 공고를 올린 회사 이름입니다. (예: "카카오", "네이버")
    // 특징: 별도 테이블 조인 없이 바로 글자를 저장해서 조회 속도를 높입니다.
    private String companyName; // 회사 이름.

    // [12] 필드(companyType): 기업 형태
    // 의미: 회사의 규모나 종류입니다. (예: "대기업", "중견기업", "외국계")
    // 특징: 크롤링할 때 정보가 없으면 "정보없음" 등으로 채워질 수 있습니다.
    private String companyType; // 기업 형태 (중소, 중견 등).

    // [13] 필드(description): 상세 내용 또는 지역 정보
    // 의미: 원래는 본문 내용이지만, 크롤링 데이터의 경우 본문을 다 긁어오기 힘들어서 주로 '근무 지역(서울 강남구...)' 정보를 저장합니다.
    private String description; // 근무 지역 등의 설명.

    // [14] 필드(careerLevel): 요구 경력
    // 의미: 지원 가능한 경력 조건입니다. (예: "신입", "경력 3년↑", "무관")
    private String careerLevel; // 경력 (신입, 경력 등).

    // [15] 필드(education): 요구 학력
    // 의미: 지원 가능한 학력 조건입니다. (예: "대졸(4년)", "학력무관")
    private String education; // 학력 (대졸, 초대졸 등).

    // [16] 필드(deadline): 마감일 (★중요)
    // 의미: 언제까지 지원 가능한지 나타냅니다.
    // 타입: 날짜(Date)가 아니라 문자열(String)입니다.
    // 이유: "2025-12-31" 같은 날짜만 있는 게 아니라, "채용시 마감", "상시 채용", "D-Day" 같은 문자가 들어오기 때문입니다.
    private String deadline; // 마감일 (String으로 저장).

    // [17] 필드(link): 원본 공고 링크 (★중요)
    // 의미: 클릭하면 사람인 사이트의 실제 공고 페이지로 이동하는 주소(URL)입니다.
    // 용도: 사용자가 "지원하기" 버튼을 누르면 이 주소로 보냅니다.
    private String link;

    // [18] 필드(createdAt): 수집 일시
    // 의미: 이 데이터가 우리 DB에 언제 크롤링되어 저장됐는지 기록합니다.
    // 용도: 너무 오래된 공고를 삭제하거나 관리할 때 씁니다.
    private LocalDateTime createdAt;

    // [19] 필드(isActive): 활성화 상태
    // 의미: 1이면 "진행중(보여줌)", 0이면 "마감됨(안 보여줌)"
    // 타입: DB에서는 참/거짓을 보통 1/0 숫자로 관리하기 때문에 int를 씁니다.
    private int isActive;
}

//
//        데이터 수집 (Crawling):
//
//        우리 서버의 크롤러(JobCrawlerService)가 사람인 사이트에 접속합니다.
//
//        "삼성전자 신입 공채", "마감일: 상시채용", "링크: www.saramin..." 같은 텍스트를 긁어옵니다.
//
//        객체 생성 (JobPost):
//
//        긁어온 텍스트들을 이 JobPost 클래스에 담습니다.
//
//        new JobPost(..., title="삼성전자...", deadline="상시채용", link="...")
//
//        DB 적재 (Insert):
//
//        꽉 채워진 JobPost 객체는 매퍼(JobMapper)에게 전달됩니다.
//
//        매퍼는 이 객체를 보고 INSERT INTO JOB_POST ... 쿼리를 날려 DB에 영구 저장합니다.
//
//        활용:
//
//        나중에 사용자가 "삼성"을 검색하면, DB에서 이 데이터를 다시 꺼내서 DTO로 변환해 보여줍니다.