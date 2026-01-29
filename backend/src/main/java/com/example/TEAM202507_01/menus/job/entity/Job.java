package com.example.TEAM202507_01.menus.job.entity;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 엔티티(entity)' 폴더에 살고 있다는 주소입니다.
// Entity는 DB 테이블과 1:1로 매칭되는 클래스들을 모아두는 곳입니다.

// [2] 임포트: 롬복(Lombok)과 날짜 관련 자바 도구들을 가져옵니다.
import lombok.AllArgsConstructor; // 모든 필드를 채우는 생성자 자동 생성
import lombok.Builder;            // 객체 조립 도구 (Builder 패턴)
import lombok.Data;               // Getter, Setter, toString 등 만능 도구
import lombok.NoArgsConstructor;  // 빈 객체 생성자
import org.apache.ibatis.jdbc.SQL;

import java.time.LocalDate;     // 날짜 (년-월-일) - 현재 코드 필드엔 안 쓰였지만 임포트되어 있네요.
import java.time.LocalDateTime; // 날짜+시간 (년-월-일 시:분:초)

// [3] 어노테이션(@Data): 종합 선물 세트
// 의미: "아래 변수들의 Getter(값 꺼내기), Setter(값 넣기), toString(출력하기) 등을 자동으로 만들어줘."
// 효과: 코드가 간결해지고, DB에서 데이터를 꺼내와서 객체에 넣을 때 필수적입니다.
@Data

// [4] 어노테이션(@Builder): 레고 조립 도우미
// 의미: "객체를 만들 때 Job.builder().title('제목').build() 처럼 직관적으로 만들게 해줘."
// 효과: 생성자 파라미터 순서를 외우지 않아도 돼서 실수를 줄여줍니다.
@Builder

// [5] 어노테이션(@NoArgsConstructor): 빈 상자 생성기
// 의미: "파라미터 없는 기본 생성자(new Job())를 만들어줘."
// 중요: MyBatis 같은 DB 프레임워크가 데이터를 읽어올 때, 일단 빈 객체를 만들고 리플렉션으로 값을 채우기 때문에 이게 없으면 에러가 납니다.
@NoArgsConstructor

// [6] 어노테이션(@AllArgsConstructor): 꽉 찬 상자 생성기
// 의미: "모든 필드 값을 파라미터로 받는 생성자를 만들어줘."
// 중요: 위의 @Builder가 작동하려면 모든 필드를 다루는 생성자가 반드시 있어야 해서 세트로 다닙니다.
@AllArgsConstructor

// [7] 클래스 선언: "지금부터 DB의 'JOB' 테이블과 짝을 이룰 Job 클래스를 정의할게."
public class Job {

    // [8] 필드(id): 기본 키(Primary Key)
    // 의미: 이 공고의 고유 번호입니다. (예: 101번 공고)
    // 특징: Long 타입을 써서 약 900경 개까지 숫자를 커버할 수 있게 합니다.
    private Long id;            // PK

    // [9] 필드(category): 게시판 구분
    // 의미: 이 글이 어떤 게시판 글인지 구분합니다. 여기선 무조건 'JOBS'가 들어갑니다.
    private String category;    // 'JOBS'

    // [10] 필드(title): 공고 제목
    // 예시: "2026년 상반기 신입 개발자 공개 채용"
    private String title;       // 제목

    // [11] 필드(companyName): 회사 이름
    // 중요: 원래는 'Company' 테이블의 ID(숫자)를 저장하고 조인(Join)하는 게 정석일 수 있습니다.
    // 하지만 [변경] 주석처럼, 조회할 때마다 조인하면 느려지거나 복잡해지니까
    // 그냥 "네이버"라는 글자를 여기에 직접 저장해서 성능과 편의성을 챙긴 구조입니다. (역정규화)
    private String companyName; // 회사 이름

    // [12] 필드(companyType): 회사 업종/형태
    // 예시: "IT/웹", "제조업", "중견기업" 등
    private String companyType; // 회사 직종

    // [13] 필드(description): 공고 상세 내용
    // 특징: DB에서는 매우 긴 글이 들어갈 수 있으므로 VARCHAR2(2000)이나 CLOB 타입으로 잡혀있을 겁니다.
    private String description; // 내용 (VARCHAR2 2000)

    // [14] 필드(careerLevel): 요구 경력
    // 예시: "신입", "경력 3년 이상", "무관"
    private String careerLevel; // 경력

    // [15] 필드(education): 요구 학력
    // 예시: "대졸(4년)", "초대졸", "학력무관"
    private String education;   // 학력

    // [16] 필드(deadline): 마감일
    // 특징: 날짜 타입(Date)이 아니라 문자열(String)입니다.
    // 이유: "2025-12-31" 같은 날짜뿐만 아니라 "채용시 마감", "상시 채용" 같은 문구도 넣어야 하기 때문에 유연하게 String을 씁니다.
    private String deadline; // 마감일

    // [17] 필드(isActive): 공고 활성화 여부
    // 의미: 1이면 "모집중(보여줘)", 0이면 "마감됨(숨겨줘)"
    // 타입: DB에서는 true/false 대신 1/0 숫자로 저장하는 경우가 많아 Integer를 씁니다.
    private Integer isActive;   // 마감여부 (1:모집중, 0:마감)

    // [18] 필드(createdAt): 생성일시
    // 의미: 이 데이터가 DB에 처음 저장된 날짜와 시간입니다.
    // 타입: LocalDateTime을 쓰면 자바가 알아서 년-월-일 시:분:초를 관리해줍니다.
    private LocalDateTime createdAt; // 생성일
}



//        상황: 관리자가 "2026년 신입 개발자 채용" 공고를 등록할 때
//
//        데이터 입력: 관리자가 제목, 내용, 회사명 등을 입력하고 [등록] 버튼을 누릅니다.
//
//        객체 생성 (Entity):
//
//        서비스 계층에서 이 입력값들을 받아 Job 객체를 만듭니다.
//
//        new Job(null, "JOBS", "신입채용", "네이버", ...) 이런 식으로 데이터를 채웁니다.
//
//        DB 저장:
//
//        이 객체가 매퍼(Mapper)에게 전달됩니다.
//
//        매퍼는 이 객체의 필드들을 읽어서 INSERT INTO JOB ... SQL 문을 만들고 DB에 영구 저장합니다.
//
//        결과:
//
//        DB의 JOB 테이블에 새로운 행(Row)이 하나 추가됩니다.