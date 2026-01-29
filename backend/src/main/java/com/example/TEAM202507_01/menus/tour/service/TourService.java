package com.example.TEAM202507_01.menus.tour.service;

import com.example.TEAM202507_01.menus.tour.dto.TourDto;
import java.util.List;

public interface TourService {
    // 제공할 기능 목록 정의
    List<TourDto> findAll();
    TourDto findById(Long id);
    TourDto save(TourDto tour); // 저장 후 저장된 객체 반환
    void delete(Long id);
}