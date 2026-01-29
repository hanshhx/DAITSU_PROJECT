package com.example.TEAM202507_01.search.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.List;

@Data
@Builder
@Document(indexName = "restaurant") // 'restaurant' 인덱스 사용
public class RestaurantDocument {

    @Id
    private Long id; // 식당 ID

    // [검색용 필드 - Text + nori]
    // 사용자가 "대전 맛집" 이라고 검색했을 때 걸려야 하는 필드들임.
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;            // 가게 이름 (가장 중요)

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address;         // 주소 (예: "유성구" 검색 시 매칭)

    @Field(type = FieldType.Text, analyzer = "nori")
    private String bestMenu;        // 대표 메뉴

    // List<String>인데 Text 타입인 이유:
    // 메뉴 하나하나(예: "짜장면", "짬뽕")를 검색 엔진이 읽어서 검색 가능하게 만들기 위함임.
    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> menu;

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> menuDetail; // 메뉴 설명

    @Field(type = FieldType.Text, analyzer = "nori")
    private String restCategory;    // '한식', '중식' 등

    // [필터/출력용 필드 - Keyword]
    // 검색보다는 화면에 보여주거나, 정확히 일치하는 값으로 거를 때 사용함.
    @Field(type = FieldType.Keyword)
    private String phone;           // 전화번호 (부분 검색 안 함)

    @Field(type = FieldType.Keyword)
    private String openTime;        // 영업시간

    @Field(type = FieldType.Keyword)
    private List<String> price;     // 가격 정보

    @Field(type = FieldType.Keyword)
    private String url;             // 링크

    @Field(type = FieldType.Keyword)
    private String imagePath;       // 이미지 경로 (분석 불필요)
}
