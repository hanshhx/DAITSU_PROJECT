package com.example.TEAM202507_01.menus.news.service;

import com.example.TEAM202507_01.menus.news.dto.NewsDto;
import java.util.List;

public interface NewsService {
    // 모든 뉴스 목록을 가져오는 기능 정의
    List<NewsDto> findAll();

    // 특정 뉴스 하나를 가져오는 기능 정의
    NewsDto findById(Long id);

    // 뉴스를 저장하거나 수정하는 기능 정의 (save 하나로 처리 예정)
//    void save(NewsDto news);

    // 뉴스를 삭제하는 기능 정의
    void delete(Long id);

    Object getDaejeonNews(String query, int display, int start);
}