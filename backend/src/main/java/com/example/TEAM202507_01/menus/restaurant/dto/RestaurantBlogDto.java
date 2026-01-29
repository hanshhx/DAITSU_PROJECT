package com.example.TEAM202507_01.menus.restaurant.dto;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 데이터 가방(dto)' 폴더에 위치한다는 주소입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.restaurant.service.RestaurantBlogService;
import com.fasterxml.jackson.annotation.JsonPropertyOrder; // JSON으로 변환할 때 순서를 정해주는 도구
import lombok.Data; // Getter, Setter 등을 자동으로 만들어주는 롬복 도구
import java.util.List; // 여러 개의 데이터를 담을 리스트 도구

@Data
// [3] 어노테이션(@Data): 롬복의 종합 선물 세트입니다.
// 의미: "이 클래스의 모든 변수에 대해 값 넣기(Set), 값 꺼내기(Get), 출력하기(ToString) 기능을 자동으로 만들어줘."
public class RestaurantBlogDto {

    // [4] 필드(total): 검색 결과 총 개수
    // 의미: 네이버에 이 키워드로 된 글이 총 몇 개 있는지 알려줍니다. (예: 15,430건)
    // 용도: "검색 결과 총 1만 건" 처럼 화면에 보여줄 때 씁니다.
    private int total;

    // [5] 필드(start): 시작 위치
    // 의미: 이번에 가져온 결과가 몇 번째 글부터인지 알려줍니다. (예: 1이면 1등부터, 11이면 11등부터)
    // 용도: 페이지네이션(1페이지, 2페이지...) 계산할 때 씁니다.
    private int start;

    // [6] 필드(display): 한 번에 보여줄 개수
    // 의미: 한 페이지에 몇 개를 가져왔는지입니다. (예: 10개)
    private int display;

    // [7] 필드(items): 실제 블로그 글 목록
    // 의미: 검색된 블로그 글 하나하나(BlogItem)를 담고 있는 리스트(바구니)입니다.
    // 구조: [ {글1}, {글2}, {글3} ... ] 형태로 들어있습니다.
    private List<BlogItem> items;

    // =========================================================
    // [내부 클래스] 블로그 글 정보 (BlogItem)
    // =========================================================
    // 이 클래스를 따로 파일로 안 만들고 안에 넣은 이유는,
    // 오직 RestaurantBlogDto 안에서만 쓰이는 부속품이기 때문입니다.

    @Data // 얘도 Getter, Setter 자동 생성
    @JsonPropertyOrder({ "title", "bloggername", "description", "postdate", "link", "thumbnail"})
    // [8] 어노테이션(@JsonPropertyOrder): JSON 순서 정하기
    // 의미: "프론트엔드한테 데이터를 보낼 때, JSON 필드 순서를 title -> bloggername -> ... 순서로 예쁘게 정렬해서 보내줘."
    // 효과: 데이터 보는 사람이 헷갈리지 않게 가독성을 높여줍니다. (기능엔 영향 없음)
    public static class BlogItem {
        // static을 붙인 이유: 바깥쪽 클래스(RestaurantBlogDto) 객체 없이도 이 모양 틀을 쓸 수 있게 하기 위함입니다.
        // (DTO 구조 잡을 때 관례적으로 많이 씁니다.)

        // [9] 필드(title): 블로그 글 제목
        // 예시: "대전 <b>성심당</b> 본점 방문 후기" (네이버는 검색어를 <b>태그로 감싸서 줍니다)
        private String title;       // 글 제목

        // [10] 필드(bloggername): 블로거 이름
        // 예시: "맛집탐방러 김씨"
        private String bloggername; // 블로거 이름

        // [11] 필드(description): 글 요약(본문 앞부분)
        // 예시: "오늘은 대전의 명물 튀김소보로를 먹으러..."
        private String description; // 글 요약

        // [12] 필드(postdate): 작성일
        // 형식: "20250701" 처럼 문자열로 옵니다.
        private String postdate;    // 작성일

        // [13] 필드(link): 원본 글 링크
        // 예시: "https://blog.naver.com/user/1234..."
        // 용도: 사용자가 클릭하면 실제 블로그로 이동시킬 때 씁니다.
        private String link;        // 글 링크

        // [14] 필드(thumbnail): 대표 이미지 주소 (★중요★)
        // 특징: 네이버 검색 API는 원래 이미지를 안 줍니다.
        // 그래서 처음엔 비어있지만(null), 우리 서비스(Service)가 크롤링해서 여기에 이미지 주소를 채워 넣습니다.
        private String thumbnail;
    }
}
//
//상황: 사용자가 "성심당"의 블로그 후기를 보고 싶어 할 때
//
//요청 (Request):
//
//우리 서버가 네이버 서버에게 묻습니다. "검색어 '대전 성심당', 블로그 글 5개만 줘."
//
//네이버 응답 (Naver API Response):
//
//네이버가 JSON으로 답합니다.
//
//        {"total": 15000, "items": [ {"title": "튀소 존맛", "link": "..."}, ... ] }
//
//데이터 담기 (Mapping):
//
//스프링이 이 JSON을 보고 RestaurantBlogDto 객체를 만듭니다.
//
//total 변수에 15000을 넣고, items 리스트에 블로그 글들을 하나하나 채워 넣습니다.
//
//썸네일 추가 (Scraping):
//
//네이버는 썸네일 이미지를 안 줍니다.
//
//그래서 우리 서비스(RestaurantBlogService)가 각 link를 타고 들어가서 이미지를 훔쳐(?)옵니다. 그리고 thumbnail 필드에 채워 넣습니다.
//
//전달 (Response):
//
//꽉 채워진 이 DTO 상자가 프론트엔드(화면)로 전달되어, 사용자에게 예쁜 사진과 함께 리뷰 목록이 뜹니다.