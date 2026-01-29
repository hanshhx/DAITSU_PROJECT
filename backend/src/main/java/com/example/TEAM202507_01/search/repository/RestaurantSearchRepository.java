package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.RestaurantDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

// [클래스/인터페이스 선언]
// interface: 구현 클래스를 우리가 만들지 않음. 스프링 데이터 엘라스틱서치가 실행 시점에 자동으로 구현체를 만들어줌 (마법).
// extends ElasticsearchRepository<RestaurantDocument, Long>:
//    - RestaurantDocument: 이 레포지토리는 '식당 문서'를 다룬다.
//    - Long: 식당 문서의 ID(@Id) 타입은 Long이다.
//    - 이렇게 상속받으면 save(), findById(), delete() 같은 기본 메서드가 공짜로 생김.
public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantDocument, Long> {

    // [커스텀 검색 쿼리 메서드]
    // @Query: 기본 메서드로는 복잡한 검색(가중치, 필드 여러 개)이 어려워서 직접 ES 쿼리(JSON)를 작성함.
    @Query("{" +
            "\"multi_match\": {" +  // multi_match: 여러 필드에서 동시에 검색하겠다는 명령어.
            "   \"query\": \"?0\", " + // ?0: 메서드의 첫 번째 파라미터(String keyword)가 여기에 들어감.
            "   \"fields\": [\"name^2\", \"address\", \"menu\", \"bestMenu\", \"menuDetail\", \"restCategory\"], " +
            // fields: 검색할 대상 필드들임.
            // "name^2": 이름(name) 필드에서 검색어가 발견되면 점수(score)를 2배로 쳐줘라! (정확도 향상)
            // 즉, "짜장면"을 검색했을 때 메뉴에 있는 것보다 가게 이름이 "짜장면집"인 게 상단에 뜸.

            "   \"type\": \"cross_fields\", " +
            // type: "cross_fields": 여러 필드를 마치 하나의 큰 필드처럼 취급해서 검색함.
            // 예를 들어 "대전 맛집"을 검색하면 "address"에 대전이 있고 "name"에 맛집이 있어도 찾아줌.

            "   \"operator\": \"and\"" +
            // operator: "and": 검색어의 모든 단어가 포함되어야 함.
            // "대전 짜장면" 검색 시 -> 대전(O) AND 짜장면(O)인 문서만 찾음. (OR로 하면 너무 많이 나옴)
            "}" +
            "}")
    // 메서드 선언: 키워드를 받아서 식당 문서 리스트를 반환함.
    List<RestaurantDocument> searchByKeyword(String keyword);
}