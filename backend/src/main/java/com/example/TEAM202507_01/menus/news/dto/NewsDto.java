package com.example.TEAM202507_01.menus.news.dto;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

//// @Getter: 모든 필드의 값을 꺼내는 메서드(getXXX)를 자동 생성함.
///@Setter: 모든 필드에 값을 넣는 메서드(setXXX)를 자동 생성함.
//@NoArgsConstructor: 파라미터가 없는 기본 생성자(public NewsDto() {})를 생성함. JPA나 라이브러리가 객체를 만들 때 필요함
//@AllArgsConstructor: 모든 필드를 파라미터로 받는 생성자를 생성함.
// @Builder: 빌더 패턴(NewsDto.builder().title("...").build())을 사용할 수 있게 해줌. 객체 생성을 직관적으로 도와줌.
// @ToString: 객체를 출력할 때(System.out.println) 필드값을 예쁘게 보여주는 메서드를 자동 생성함.

@Data
public class NewsDto {
    // 1. 검색 결과 공통 정보 (rss/channel 하위 요소들)
    private String lastBuildDate; // 검색 결과를 생성한 시간
    private Integer total;        // 총 검색 결과 개수
    private Integer start;        // 검색 시작 위치
    private Integer display;      // 한 번에 표시할 검색 결과 개수

    // 2. 개별 검색 결과 리스트 (rss/channel/item 요소들)
    private List<NewsItem> items; // JSON에서는 items라는 이름의 배열로 옵니다.

    @Data
    public static class NewsItem {
        private String title;          // 뉴스 기사의 제목 (<b> 태그 포함)
        private String originallink;   // 뉴스 기사 원문 URL
        private String link;           // 네이버 뉴스 URL
        private String description;    // 뉴스 요약 내용 (<b> 태그 포함)
        private String pubDate;        // 뉴스 기사가 제공된 시간
        private String thumbnail;
    }


}