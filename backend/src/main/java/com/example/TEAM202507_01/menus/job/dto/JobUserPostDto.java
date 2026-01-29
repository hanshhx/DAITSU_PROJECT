package com.example.TEAM202507_01.menus.job.dto;
// [1] 패키지 선언: 위와 동일한 위치입니다.

import lombok.*; // 롬복 도구 가져오기

// [2] 어노테이션: 위와 동일하게 편리한 기능들을 장착합니다.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobUserPostDto {
    // [3] 필드 정의: 사용자가 작성한 구직 글 정보를 담습니다.

    private Long id;
    // 의미: 글 번호.

    private String category;
    // 의미: 카테고리 (보통 "JOBS" 또는 "USER_JOB" 등으로 구분)

    private String userId;
    // 의미: 작성자 아이디 (예: "user123")
    // 용도: 누가 쓴 글인지 알아야 수정/삭제 권한을 줄 수 있습니다.

    private String title;
    // 의미: 글 제목 (예: "성실한 풀스택 개발자입니다!")

    private String companyName;
    // 의미: 회사 이름?
    // 특이사항: 구직자가 글을 쓰는 거라 회사 이름은 없지만,
    // DB 테이블(`JOBS`) 하나를 같이 쓰다 보니 구조를 맞추려고 넣어둔 필드입니다. (보통 비워둡니다.)

    private String companyType;
    // 의미: 희망 기업 형태 (예: "스타트업 선호")

    private String description;
    // 의미: 자기소개 내용 본문.
    // 예시: "저는 자바와 스프링을 잘 다루며..."

    private String careerLevel;
    // 의미: 나의 경력 (예: "신입", "인턴 6개월")

    private String education;
    // 의미: 나의 학력 (예: "컴퓨터공학 전공")

    // [4] 날짜 필드: 프론트엔드와 주고받을 땐 String이 가장 안전합니다.
    // 복잡한 날짜 객체(Date)로 주고받으면 시차 문제나 포맷 에러가 날 수 있어서, 그냥 문자로 주고받습니다.
    private String deadline;
    // 의미: 구직 마감일 (언제까지 일자리를 구할 건지)

    private String createdAt;
    // 의미: 작성일 (이 글을 언제 썼는지)

    private int isActive;
    // 의미: 구직 상태 (1: 구직중, 0: 구직 완료/비공개)
}