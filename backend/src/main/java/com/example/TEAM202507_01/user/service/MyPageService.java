package com.example.TEAM202507_01.user.service;

import com.example.TEAM202507_01.user.dto.MyPageDto;
import java.util.List;
import java.util.Map;

public interface MyPageService {
    MyPageDto getMyInfo(String loginId);
    void updateMyInfo(MyPageDto dto);

    List<Map<String, Object>> getMyPosts(String loginId, int offset, int size);
    List<Map<String, Object>> getMyComments(String loginId, int offset, int size);
    List<Map<String, Object>> getMyFavorites(String loginId, int offset, int size);

    void updatePost(Long id, String loginId, String title, String content);
    void updateComment(Long id, String loginId, String content);

    void deletePost(Long id, String loginId);
    void deleteComment(Long id, String loginId);
    void deleteFavorite(Long id, String loginId);
}