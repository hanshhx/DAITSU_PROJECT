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
public class TourPostLikeDto {
    private String userId; // 댓글 작성자 id user_id
    private Long postId; // 게시글 id post_id
    private LocalDateTime createdAt; // 등록일시 created_at
}
