package com.example.TEAM202507_01.menus.tour.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TourPostCommentDto {
    private Long reviewId;      // 댓글(리뷰) 고유 ID
    private Long postId;        // 어떤 게시글에 달린 댓글인지 (FK)
    private String userId;      // 댓글 작성자
    private String content;     // 댓글 내용
    private boolean isDelete;   // 삭제 여부 (실제 삭제 대신 '삭제됨' 표시만 할 때 사용)
    private LocalDateTime createdAt; // 작성 시간
}