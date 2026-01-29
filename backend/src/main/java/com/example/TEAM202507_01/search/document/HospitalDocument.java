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
@Document(indexName = "hospital") // 'hospital' 인덱스
public class HospitalDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address;

    // "내과", "피부과" 등으로 검색해야 하므로 Text 타입 + nori 분석기 사용.
    @Field(type = FieldType.Text, analyzer = "nori")
    private String treatCategory; // 진료과목

    @Field(type = FieldType.Keyword)
    private String tel;

    // 날짜 처리를 위한 설정
    // pattern: 다양한 날짜 형식을 받아들일 수 있도록 지정함 (연-월-일 시:분:초 등).
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime editDate;

    // 정렬(Sorting)을 위해 숫자 타입(Double, Integer)을 명시적으로 사용함.
    @Field(type = FieldType.Double)
    private Double averageRating; // 별점 (소수점)

    @Field(type = FieldType.Integer)
    private Integer reviewCount;  // 리뷰 수
}
