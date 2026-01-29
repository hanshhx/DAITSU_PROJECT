package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@Document(indexName = "job_user_post")
public class JobUserPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String companyName; // 희망 기업? 혹은 경력 기업

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description; // 자기소개 등

    // 필터링
    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String companyType;

    @Field(type = FieldType.Keyword)
    private String careerLevel;

    @Field(type = FieldType.Keyword)
    private String education;

    @Field(type = FieldType.Keyword)
    private String deadline;

    @Field(type = FieldType.Keyword)
    private String createdAt;

    @Field(type = FieldType.Integer)
    private int isActive;
}