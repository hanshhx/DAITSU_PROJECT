package com.example.TEAM202507_01.menus.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data // @Getter, @Setter, @ToString 등을 합친 종합 선물 세트.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;            // 댓글 고유 번호 (PK).
    private Long postId;        // [핵심] 어떤 게시글에 달린 댓글인지 연결 (FK).
    private String userId;      // 댓글 작성자 아이디.
    private String userNickname;// 댓글 작성자 닉네임 (화면 표시용).
    private String content;     // 댓글 내용.

    // 댓글을 진짜 삭제하면 DB에서 사라지지만, 보통은 '삭제된 댓글입니다'라고 띄우기 위해 상태값만 바꿈.
    // 0: 정상, 1: 삭제됨.
    private Integer isDelete;

    private LocalDateTime createdAt; // 작성 시간.
    private Long parentId;      // [대댓글] 이 댓글의 부모 댓글 ID (null이면 일반 댓글).
}

