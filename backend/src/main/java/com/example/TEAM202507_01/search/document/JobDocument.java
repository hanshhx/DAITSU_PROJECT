package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Document(indexName = "job")
public class JobDocument {

    @Id
    private Long id;

    // 검색 대상
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;       // 공고 제목

    @Field(type = FieldType.Text, analyzer = "nori")
    private String companyName; // 회사명

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description; // 상세 내용 (긴 글)

    @Field(type = FieldType.Text, analyzer = "nori")
    private String companyType; // 기업 형태

    @Field(type = FieldType.Text, analyzer = "nori")
    private String category;    // 직무 카테고리

    // 필터링 전용 (Keyword) - "신입", "경력" 처럼 딱 떨어지는 값
    @Field(type = FieldType.Keyword)
    private String careerLevel;

    @Field(type = FieldType.Keyword)
    private String education;   // 학력

    @Field(type = FieldType.Keyword)
    private String deadline;    // 마감일

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Integer)
    private int isActive;       // 공고 진행 중 여부 (1 or 0)
}
