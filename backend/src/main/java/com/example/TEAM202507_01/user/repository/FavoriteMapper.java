package com.example.TEAM202507_01.user.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper {
    int exists(String category, String userId, Long fovId);
    void insert(String category, String userId, Long fovId);
    void delete(String category, String userId, Long fovId);
}