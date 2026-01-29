package com.example.TEAM202507_01.menus.hospital.dto;
// [1] 패키지 선언: 이 파일이 '병원 메뉴 > 데이터 가방(dto)' 폴더 안에 있다는 주소입니다.

// [2] 임포트: 롬복(Lombok)과 날짜 관련 도구들을 가져옵니다.
import lombok.AllArgsConstructor; // 모든 재료를 다 넣는 생성자 제조기
import lombok.Builder;            // 객체를 순서 상관없이 예쁘게 조립하는 도구
import lombok.Data;               // Getter, Setter, toString 자동 생성기
import lombok.NoArgsConstructor;  // 빈 깡통 객체 생성기

import java.time.LocalDate;     // 날짜(년-월-일) 도구 (현재 코드에선 안 쓰이지만 임포트 되어있네요)
import java.time.LocalDateTime; // 날짜+시간(년-월-일 시:분:초) 도구

// [3] 어노테이션: 이 클래스에 편리한 기능들을 부여합니다.
@Data
// 의미: "롬복아, 아래 변수들에 대해 값 넣기(Set), 꺼내기(Get), 출력하기(ToString) 기능 다 만들어줘."

@NoArgsConstructor
// 의미: "파라미터 없는 기본 생성자(new HospitalReviewDto())를 만들어줘."
// 용도: DB에서 데이터를 꺼내오거나, 프론트에서 JSON을 받을 때 일단 빈 객체를 만들어야 해서 필수입니다.

@AllArgsConstructor
// 의미: "모든 필드 값을 한 번에 다 채우는 생성자를 만들어줘."
// 용도: 아래 @Builder가 작동하려면 이게 꼭 있어야 합니다.

@Builder
// 의미: "객체를 만들 때 HospitalReviewDto.builder().content("최고").rating(5).build() 처럼 직관적으로 만들게 해줘."
public class HospitalReviewDto {
    // [4] 클래스 선언: 병원 리뷰 데이터를 담을 그릇을 정의합니다.

    private Long Id;
    // 의미: 리뷰의 고유 관리 번호 (PK)
    // 용도: 리뷰를 수정하거나 삭제할 때 "몇 번 리뷰 지워줘"라고 말하려면 주민번호 같은 고유 키가 필요합니다.
    // (참고: 보통 자바 변수명은 소문자 id로 시작하는데, 여기선 대문자 Id로 되어있네요. 기능상 문제는 없지만 관례는 소문자입니다.)

    private Long hospitalId;
    // 의미: 어느 병원에 쓴 리뷰인지 (FK)
    // 용도: "이 리뷰가 '대전 한국병원(ID:1)'에 달린 건지, '둔산 내과(ID:2)'에 달린 건지" 연결해주는 연결 고리입니다.

    private String userId;
    // 의미: 작성자 아이디
    // 용도: 누가 썼는지 기록합니다. (예: "user123")
    // 나중에 "내가 쓴 리뷰만 보기" 기능을 만들 때 필요합니다.

    private String userNickname;
    // 의미: 화면에 보여줄 작성자 닉네임
    // 용도: 아이디("user123")를 그대로 보여주면 개인정보 문제도 있고 안 예쁘니까, "건강왕" 같은 닉네임을 담습니다.
    // 꿀팁: DB 리뷰 테이블에는 닉네임이 없을 수도 있지만, 조회할 때 유저 테이블이랑 조인해서 여기에 담아오면 화면 처리가 편해집니다.

    private String content;
    // 의미: 리뷰 내용 본문
    // 예시: "의사 선생님이 정말 친절하시고 설명도 잘 해주세요!"

    private Integer rating;
    // 의미: 별점
    // 타입: Integer (정수)
    // 예시: 5 (별 5개), 1 (별 1개)
    // 용도: 나중에 병원의 '평균 별점'을 계산할 때 사용됩니다.

    private LocalDateTime createdAt;
    // 의미: 작성일시
    // 타입: LocalDateTime (년-월-일 시:분:초)
    // 용도: "2025-07-01 14:30에 작성됨" 처럼 최신순 정렬을 하거나 언제 쓴 글인지 보여줄 때 씁니다.
}
//
//        리뷰 작성 (Write):
//
//        사용자가 "치료가 빨라요"라고 쓰고 별점 5개를 줍니다.
//
//        이 정보가 서버로 날아오면, 서버는 이 HospitalReviewDto 상자에 담습니다.
//
//        "작성자는 user1, 병원은 3번 병원, 내용은 치료가 빨라요, 별점 5..." 이렇게 채워서 DB에 저장합니다.
//
//        리뷰 조회 (Read):
//
//        다른 사용자가 3번 병원을 클릭합니다.
//
//        서버는 DB에서 3번 병원에 달린 리뷰들을 꺼냅니다.
//
//        이때 userNickname("건강지킴이") 같은 정보도 같이 이 상자에 담아서 화면으로 보냅니다.
//
//        화면에는 "건강지킴이: 치료가 빨라요 ⭐⭐⭐⭐⭐ (2025-07-01)" 이렇게 예쁘게 표시됩니다.