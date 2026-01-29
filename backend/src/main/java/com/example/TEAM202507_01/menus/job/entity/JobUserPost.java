package com.example.TEAM202507_01.menus.job.entity;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 엔티티(entity)' 폴더에 살고 있다는 주소입니다.
// "여기는 DB 테이블과 똑같이 생긴 원본 데이터 클래스들이 모여사는 동네야"라고 알려주는 겁니다.

// [2] 임포트: 롬복(Lombok) 라이브러리와 날짜 관련 도구들을 가져옵니다.
import lombok.*; // @Getter, @Builder 등을 쓰기 위한 도구 상자

import java.time.LocalDateTime; // 날짜와 시간(년-월-일 시:분:초)을 다루는 자바 도구
import java.util.Date;          // 옛날 날짜 도구 (여기선 안 쓰이지만 임포트되어 있음)

// [3] 어노테이션(@Getter): 읽기 전용 기능 추가
// 의미: "이 클래스 안의 모든 변수(id, title 등)에 대해 값을 꺼내는 getTitle() 같은 메서드를 자동으로 만들어줘."
// 효과: 다른 클래스에서 이 객체 안에 든 데이터를 읽어볼 수 있게 해줍니다.
@Getter

// [4] 어노테이션(@NoArgsConstructor): 빈 상자 생성기
// 의미: "내용물이 아무것도 없는 텅 빈 객체(new JobUserPost())를 만드는 생성자를 추가해줘."
// 중요: DB에서 데이터를 꺼내올 때(MyBatis), 일단 빈 상자를 먼저 만들고 나서 데이터를 채우기 때문에 이게 없으면 에러가 납니다. 필수!
@NoArgsConstructor

// [5] 어노테이션(@AllArgsConstructor): 꽉 찬 상자 생성기
// 의미: "모든 필드 값을 한 번에 다 채워넣는 생성자를 추가해줘."
// 중요: 아래 @Builder 기능을 쓰려면 전체 생성자가 꼭 필요하기 때문에 짝꿍으로 붙여줍니다.
@AllArgsConstructor

// [6] 어노테이션(@Builder): 조립식 생성 도우미
// 의미: "객체를 만들 때 JobUserPost.builder().title('제목').build() 처럼 이름표를 붙여서 만들게 해줘."
// 효과: 필드가 많을 때 순서를 헷갈리지 않고 정확하게 데이터를 넣을 수 있습니다.
@Builder

// [7] 클래스 선언: "지금부터 사용자 구직 공고 원본 데이터(JobUserPost)를 정의할게."
public class JobUserPost {
    // 위와 동일하게 DB 테이블 구조를 그대로 옮겨놓은 클래스임.
    // 날짜 관련 필드는 DB와의 호환성을 위해 String이나 LocalDateTime을 상황에 맞춰 사용함.

    // [8] 필드(id): 글 번호 (Primary Key)
    // 의미: 이 구직 글의 고유 등록 번호입니다. (예: 501번 게시글)
    // 용도: 수정하거나 삭제할 때 "501번 글 지워줘"라고 지목하기 위해 씁니다.
    private Long id;

    // [9] 필드(category): 게시판 구분
    // 의미: 이 글이 어떤 종류인지 표시합니다. 보통 "USER_JOB" 혹은 "JOBS" 등이 들어갑니다.
    private String category;

    // [10] 필드(userId): 작성자 아이디
    // 의미: "누가 쓴 글이야?"에 대한 정보입니다. (예: "user123")
    // 중요: 이게 있어야 나중에 "본인만 수정/삭제 가능" 기능을 만들 수 있습니다.
    private String userId;

    // [11] 필드(title): 글 제목
    // 예시: "성실하고 끈기 있는 신입 개발자입니다."
    private String title;

    // [12] 필드(companyName): 희망 회사명? (DB 구조 맞춤용)
    // 의미: 구직자가 글을 쓰는 거라 회사 이름은 딱히 없지만,
    // DB의 'JOB' 테이블 구조를 재사용하거나 통일성을 위해 남겨둔 필드입니다. 보통 비워두거나 "무관"으로 채웁니다.
    private String companyName;

    // [13] 필드(companyType): 희망 기업 형태
    // 의미: 어떤 종류의 회사를 가고 싶은지 적습니다. (예: "스타트업", "대기업", "상관없음")
    private String companyType;

    // [14] 필드(description): 자기소개 본문
    // 의미: 자신의 강점, 기술 스택 등을 자세히 적는 공간입니다. DB에는 긴 글(CLOB/TEXT)로 저장됩니다.
    private String description;

    // [15] 필드(careerLevel): 나의 경력
    // 의미: 현재 자신의 경력 상태를 적습니다. (예: "신입", "경력 2년")
    private String careerLevel;

    // [16] 필드(education): 나의 학력
    // 의미: 최종 학력을 적습니다. (예: "대학교 졸업", "고등학교 졸업")
    private String education;

    // 🟢 DB에서 이미 문자열이거나, 자동 변환을 위해 String 사용
    // [17] 필드(deadline): 구직 마감일
    // 의미: "언제까지 구직 활동을 할 건지" 날짜입니다.
    // 타입: String을 쓴 이유는 프론트엔드에서 날짜를 "2025-12-31" 같은 문자열로 보내주는데,
    // 굳이 Date 객체로 변환하다가 에러 내지 말고 그냥 문자로 받아서 저장하려는 의도입니다. (안전빵)
    private String deadline;

    // [18] 필드(createdAt): 작성일
    // 의미: 이 글을 언제 썼는지 기록합니다.
    // 타입: 이것도 DB 설정에 따라 문자열(String)로 관리하고 있네요. 보통은 날짜 보여주기용으로 씁니다.
    private String createdAt;

    // [19] 필드(isActive): 구직 상태 (활성 여부)
    // 의미: 1이면 "저 아직 구직 중이에요(공개)", 0이면 "취업 성공했어요(비공개)"
    // 용도: 인재 목록에서 이미 취업한 사람을 안 보여줄 때 사용합니다.
    private int isActive;
}

//
//상황: 취준생 '김코딩'님이 구직 글을 올릴 때
//
//작성 (Write):
//
//        김코딩님이 사이트에서 "이력서 등록" 버튼을 누릅니다.
//
//        제목: "열정 가득한 신입 개발자입니다", 희망연봉: "3500만원", 기술스택: "Java, Spring" 등을 입력합니다.
//
//        객체 생성 (Entity):
//
//        서비스(Service) 계층에서 김코딩님이 입력한 내용을 바탕으로 JobUserPost 객체를 만듭니다.
//
//        new JobUserPost(..., userId="kimCoding", title="열정 가득...", isActive=1)
//
//        저장 (DB Insert):
//
//        이 객체가 매퍼(Mapper)에게 전달됩니다.
//
//        매퍼는 INSERT INTO JOB_USER_POST ... SQL을 실행해 DB에 영구 저장합니다.
//
//        결과:
//
//        이제 기업 담당자가 "인재 찾기" 메뉴를 누르면 김코딩님의 글이 조회됩니다.