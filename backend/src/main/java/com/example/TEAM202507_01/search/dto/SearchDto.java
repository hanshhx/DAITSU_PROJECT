package com.example.TEAM202507_01.search.dto;

import com.example.TEAM202507_01.search.document.*;
// 나중에 추가될 ReviewDocument 등 import...
import lombok.Builder;
import lombok.Data;

import java.util.List;

// [어노테이션 설명]
// @Data: Getter, Setter, toString 등을 자동 생성함. 데이터 전달용 객체(DTO) 필수템임.
// @Builder: SearchDto.builder().restaurants(리스트).news(리스트)...build() 처럼 객체를 예쁘게 만들 수 있음.
@Data
@Builder
public class SearchDto {
    // [필드 설명]
    // 모든 필드가 List<Document> 형태임. 검색 결과는 하나가 아니라 여러 개니까 List를 씀.

    // 1. 식당 검색 결과 리스트
    private List<RestaurantDocument> restaurants;

    // 2. 관광지 검색 결과 리스트
    private List<TourDocument> tours;

    // 3. 관광지 게시글(후기) 검색 결과 리스트
    private List<TourPostDocument> tourPosts;

    // 4. 뉴스 검색 결과 리스트
//    private List<NewsDocument> news;

    // 5. 채용 공고(기업) 검색 결과 리스트
    private List<JobDocument> jobs;

    // 6. 구직자 게시글 검색 결과 리스트
    private List<JobUserPostDocument> jobPosts;

    // 7. 병원 검색 결과 리스트
    private List<HospitalDocument> hospitals;

    // 8. 커뮤니티(자유게시판 등) 글 검색 결과 리스트
    private List<CommunityPostDocument> communityPosts;
}