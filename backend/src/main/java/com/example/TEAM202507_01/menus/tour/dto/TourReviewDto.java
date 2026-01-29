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
public class TourReviewDto {
    private Long id; // 리뷰 ID review_id
    private Long tourId; // 관광지 ID tour_id
    private String userId; // 작성자 ID user_id
    private String content; // 리뷰 내용 content_id
    private double rating; // 평점 rating
    private LocalDateTime createdAt; // 작성일시 created_at
}