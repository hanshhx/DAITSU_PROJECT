package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@Document(indexName = "tour") // 'tour' 인덱스 사용
public class TourDocument {

    @Id
    private Long id;

    // 검색 대상 (Text + nori)
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;        // 관광지명

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address;     // 지번 주소

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description; // 설명 (긴 글 안에서 키워드 검색)

    // 단순 정보 (Keyword)
    @Field(type = FieldType.Keyword)
    private String phone;
    @Field(type = FieldType.Keyword)
    private String image;
}