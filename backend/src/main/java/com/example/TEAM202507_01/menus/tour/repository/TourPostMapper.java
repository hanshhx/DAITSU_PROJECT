package com.example.TEAM202507_01.menus.tour.repository;

import com.example.TEAM202507_01.menus.tour.dto.TourPostDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TourPostMapper {
    // 모든 게시글을 조회함.
    List<TourPostDto> findAll();
}
