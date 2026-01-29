package com.example.TEAM202507_01.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

// [어노테이션 분석]
// @Getter, @Setter: 필드값 읽기/쓰기 메서드 자동 생성.
// @NoArgsConstructor: 기본 생성자 생성 (JPA/ES가 객체 만들 때 필수).
// @AllArgsConstructor: 모든 필드 포함 생성자 생성.
// @Builder: 객체 생성 시 가독성을 높여주는 패턴 제공.
// @Document(indexName = "integrated_search"):
//    - 이 클래스는 엘라스틱서치의 "integrated_search"라는 인덱스(DB의 테이블 개념)에 저장된다는 뜻임.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "integrated_search")
public class SearchDocument {

    // @Id: 엘라스틱서치에서 각 데이터를 구분하는 고유 식별자임. (RDB의 Primary Key 역할)
    // String인 이유: "NEWS_1", "JOB_50" 처럼 카테고리와 ID를 합쳐서 유니크하게 만들기 위함일 것임.
    @Id
    private String id;

    // @Field(type = FieldType.Keyword):
    //    - Keyword 타입은 텍스트를 분석(쪼개기)하지 않고 '있는 그대로' 저장함.
    //    - "NEWS", "JOB" 같이 정확하게 일치하는 값으로 필터링할 때 사용함.
    @Field(type = FieldType.Keyword)
    private String category; // 데이터 종류 구분 (NEWS, JOB, POST 등)

    @Field(type = FieldType.Long)
    private Long originalId; // 원본 DB(MySQL)에서의 PK값. 나중에 상세 페이지로 이동할 때 필요함.

    // @Field(type = FieldType.Text, analyzer = "nori"):
    //    - Text 타입은 문장을 단어 단위로 쪼개서 인덱싱함 (검색용).
    //    - analyzer = "nori": 한글 형태소 분석기인 'nori'를 사용한다는 뜻임.
    //      예) "아버지가 방에" -> "아버지", "방" 등을 추출해서 검색되게 함.
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    @Field(type = FieldType.Keyword)
    private String url; // 검색 결과 클릭 시 이동할 경로. (분석 불필요하므로 Keyword)
}