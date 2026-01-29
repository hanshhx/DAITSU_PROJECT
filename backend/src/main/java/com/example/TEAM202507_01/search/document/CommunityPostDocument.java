package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.LocalDateTime;

@Data
@Builder
@Document(indexName = "community_post")
public class CommunityPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    // 닉네임으로 검색할 수 있게 Text + nori 분석기 사용
    @Field(type = FieldType.Text, analyzer = "nori")
    private String userNickname; // 닉네임으로 검색 가능하게

    // 필터/정렬
    @Field(type = FieldType.Keyword)
    private String userId;    // 특정 유저의 글 모아보기용

    @Field(type = FieldType.Keyword)
    private String category; // 게시판 카테고리 필터

    @Field(type = FieldType.Long)
    private Long viewCount;   // 인기순 정렬

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime updatedAt;
}