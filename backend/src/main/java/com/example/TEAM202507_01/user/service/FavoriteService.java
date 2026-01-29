package com.example.TEAM202507_01.user.service;

import com.example.TEAM202507_01.user.repository.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;

    public void toggleFavorite(String category, String userId, Long fovId) {
        // 1. 이미 즐겨찾기 했는지 확인
        int count = favoriteMapper.exists(category, userId, fovId);

        if (count > 0) {
            // 이미 있으면 -> 삭제 (즐겨찾기 취소)
            favoriteMapper.delete(category, userId, fovId);
        } else {
            // 없으면 -> 추가 (즐겨찾기 등록)
            favoriteMapper.insert(category, userId, fovId);
        }
    }
}