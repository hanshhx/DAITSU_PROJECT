package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document(indexName = "tour_post")
public class TourPostDocument {

    @Id
    private Long id;

    // 검색용
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    // 댓글 내용도 검색하고 싶다면 Text로, 아니면 빼도 됩니다.
    // 여기서는 단순 출력을 위해 넣지 않거나, 필요시 List<String>으로 댓글 내용만 추출해서 넣으세요.

    // 필터/정렬용
    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long) // 조회수 정렬을 위해 Long
    private Long viewCount;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime updatedAt;
}