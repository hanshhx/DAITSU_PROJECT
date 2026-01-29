//package com.example.TEAM202507_01.search.document;
//
//import lombok.Builder;
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//import java.time.LocalDateTime;
//
//@Data
//@Builder
//@Document(indexName = "news")
//public class NewsDocument {
//
//    @Id
//    private Long id;
//
//    @Field(type = FieldType.Text, analyzer = "nori")
//    private String title;
//
//    @Field(type = FieldType.Text, analyzer = "nori")
//    private String content;
//
//    @Field(type = FieldType.Text, analyzer = "nori")
//    private String source;   // 언론사명 검색 (예: "연합뉴스")
//
//    @Field(type = FieldType.Text, analyzer = "nori")
//    private String author;   // 기자 이름 검색
//
//    @Field(type = FieldType.Keyword)
//    private String imageUrl; // 이미지 주소는 검색 안 함
//
//
//    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
//    private LocalDateTime publishedAt;  // 최신순 정렬용
//}