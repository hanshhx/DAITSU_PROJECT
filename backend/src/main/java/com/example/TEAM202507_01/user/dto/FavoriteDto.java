package com.example.TEAM202507_01.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteDto {
    private Long category;        // 카테고리
    private Long userId;    // 사용자 ID (FK) -> 토큰에서 꺼낼 값
    private Long fovId;    // 해당 ID (FK)
}