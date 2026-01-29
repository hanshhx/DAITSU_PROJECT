package com.example.TEAM202507_01.menus.community.dto; // ★ 패키지명 확인!

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

// [어노테이션 설명]
// @Getter, @Setter: 필드값을 넣고 빼는 메서드 자동 생성.
// @NoArgsConstructor: 기본 생성자 (JPA/MyBatis가 객체 생성할 때 필수).
// @AllArgsConstructor: 모든 필드를 채우는 생성자.
// @Builder: 객체 생성 시 가독성을 높여주는 패턴.
// @ToString: 로그 찍을 때 객체 내용을 문자로 보여줌.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CommunityDto {
    private Long id;            // 게시글 고유 번호 (PK).
    private String title;       // 글 제목.
    private String content;     // 글 내용.
    private LocalDateTime createdAt; // 작성일시.
    private String userId;// 작성자 아이디 (FK).
    // [수정] RECOMMEND -> NOTICE로 변경 주석 반영
    private String category;    // 게시판 종류 (FREE: 자유, review: 추천).
    private LocalDateTime updatedAt; // 수정일시.
    private Long viewCount;     // 조회수.

    // [중요] DB 테이블(POSTS)에는 없지만, 화면에 보여주기 위해 JOIN해서 가져오는 값임.
    private String userNickname; // 작성자 닉네임.
    private int commentCount;
    private int likeCount;
    private Boolean isLiked;
}