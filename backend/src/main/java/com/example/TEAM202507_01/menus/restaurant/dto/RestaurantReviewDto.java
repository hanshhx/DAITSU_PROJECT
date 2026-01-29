package com.example.TEAM202507_01.menus.restaurant.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RestaurantReviewDto {
    private Long id;            // 리뷰 고유 ID
    private Long restaurantId;  // 어떤 식당에 대한 리뷰인지 (FK)
    private String userId;      // 누가 썼는지 (작성자 ID)
    private String content;     // 리뷰 내용
    private double rating;      // 별점 (double을 써서 4.5 같은 소수점 점수 지원)
    private LocalDate createdAt;// 작성 날짜 (시간 제외 날짜만 필요해서 LocalDate 사용)
}