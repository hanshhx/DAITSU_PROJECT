package com.example.TEAM202507_01.menus.tour.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TourPostDto {
    private Long id;                // 게시글 ID
    private String userId;          // 작성자 ID (누가 썼는지)
    private String category;        // 카테고리 (질문, 후기 등)
    private String title;           // 글 제목
    private String content;         // 글 내용
    private Long viewCount;         // 조회수

    // LocalDateTime: 날짜와 시간을 모두 다루기 위해 사용함.
    private LocalDateTime createdAt; // 작성 시간
    private LocalDateTime updatedAt; // 수정 시간

    // List<TourPostCommentDto>: 게시글 하나에 댓글이 여러 개 달릴 수 있으므로 리스트로 포함시킴 (1:N 관계).
    private List<TourPostCommentDto> comments; // 이 글에 달린 댓글들
}





