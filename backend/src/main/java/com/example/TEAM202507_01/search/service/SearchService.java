package com.example.TEAM202507_01.search.service;

import com.example.TEAM202507_01.search.document.SearchDocument;
import com.example.TEAM202507_01.search.dto.SearchDto;

import java.util.List;

public interface SearchService {

    // [검색 기능]
    // 사용자가 입력한 키워드(String)를 받아서, 아까 본 SearchDto(모든 결과 모음)를 반환해야 함.
    SearchDto searchIntegrated(String keyword);


    // [데이터 마이그레이션 (DB -> ES) 기능]
    // RDB에 있는 데이터를 엘라스틱서치로 옮기는 메서드들임.

    String migrateAllData(); // 전체 데이터를 한 번에 옮기기 위한 메서드

    // 각 도메인별로 따로 옮기기 위한 메서드들
    String restaurantDtoToEs();
    String tourDtoToEs();
//    String tourPostDtoToEs();
//  String newsDtoToEs();
//    String jobUserPostDtoToEs();
    String jobDtoToEs();
    String hospitalDtoToEs();
    String communityPostDtoToEs();
}