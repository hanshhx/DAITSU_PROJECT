package com.example.TEAM202507_01.user.repository;

import com.example.TEAM202507_01.user.dto.MyPageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface MyPageMapper {
    String findUuidByLoginId(@Param("loginId") String loginId);
    MyPageDto selectUserInfo(@Param("loginId") String loginId);

    List<Map<String, Object>> selectMyPosts(@Param("userId") String userId, @Param("startRow") int startRow, @Param("endRow") int endRow);
    List<Map<String, Object>> selectMyComments(@Param("userId") String userId, @Param("startRow") int startRow, @Param("endRow") int endRow);
    List<Map<String, Object>> selectMyFavorites(@Param("userId") String userId, @Param("startRow") int startRow, @Param("endRow") int endRow);

    void updateUserInfo(MyPageDto dto);
    void updatePost(@Param("id") Long id, @Param("userId") String userId, @Param("title") String title, @Param("content") String content);
    void updateComment(@Param("id") Long id, @Param("userId") String userId, @Param("content") String content);
    void deletePost(@Param("id") Long id, @Param("userId") String userId);
    void deleteComment(@Param("id") Long id, @Param("userId") String userId);
    void deleteFavorite(@Param("id") Long id, @Param("userId") String userId);
}