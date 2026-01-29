package com.example.TEAM202507_01.menus.restaurant.dto;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 데이터 가방(dto)' 폴더에 있다는 주소입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.fasterxml.jackson.annotation.JsonAlias; // [핵심] 별명(Alias)을 붙여주는 도구. 외부 데이터 이름과 내 변수 이름을 연결해줍니다.
import com.fasterxml.jackson.annotation.JsonIgnore; // (현재 코드엔 안 쓰였지만) JSON으로 변환할 때 "이건 빼고 보내"라고 할 때 씁니다.
import com.fasterxml.jackson.annotation.JsonProperty; // JSON 키 이름을 아예 이걸로 고정하고 싶을 때 씁니다.
import lombok.AllArgsConstructor; // 모든 필드를 채우는 생성자 자동 생성
import lombok.Builder; // 객체 조립 도구
import lombok.Data; // Getter, Setter 등 만능 도구
import lombok.NoArgsConstructor; // 빈 깡통 생성자 자동 생성
import java.util.List; // 메뉴 리스트 등을 담기 위한 도구

// [3] 어노테이션(@Data): 롬복의 종합 선물 세트
// 의미: "아래 변수들에 대해 값 넣기(Set), 값 꺼내기(Get), 출력하기(ToString) 기능을 자동으로 다 만들어줘."
@Data

// [4] 어노테이션(@Builder): 레고 조립 도우미
// 의미: "객체를 만들 때 RestaurantDto.builder().name('성심당').build() 처럼 이름표 붙여서 직관적으로 만들게 해줘."
@Builder

// [5] 어노테이션(@NoArgsConstructor): 빈 상자 생성기 (필수!)
// 의미: "파라미터 없는 기본 생성자(new RestaurantDto())를 만들어줘."
// 중요: 외부 JSON 데이터를 받아서 이 객체로 변환할 때(Jackson), 일단 빈 객체를 먼저 만들고 데이터를 채우기 때문에 이게 없으면 에러 납니다.
@NoArgsConstructor

// [6] 어노테이션(@AllArgsConstructor): 꽉 찬 상자 생성기
// 의미: "모든 필드 값을 한 번에 다 채우는 생성자를 만들어줘." (@Builder 쓰려면 필수 짝꿍)
@AllArgsConstructor

// [7] 클래스 선언: "지금부터 외부 맛집 데이터를 담을 통역사 가방(RestaurantDto)을 정의할게."
public class RestaurantDto {

    // 1. 식당 고유 번호
    // [8] @JsonAlias("REST_ID"): 통역 설정
    // 상황: 외부 API(공공데이터)에서는 이 값을 "REST_ID"라는 이름으로 보내줍니다.
    // 동작: "REST_ID"라는 이름표를 단 데이터가 들어오면, 자동으로 아래 'id' 변수에 집어넣습니다.
    @JsonAlias("REST_ID")
    private Long id;                // 식당 고유 번호 (PK) - 우리 시스템에서 쓸 이름

    // 2. 식당 이름
    // [9] @JsonAlias("REST_NM"): 통역 설정
    // 상황: 외부에서는 "REST_NM" (Restaurant Name의 약어인 듯)으로 보냅니다.
    // 동작: 이걸 받아서 우리 변수 'name'에 넣습니다.
    @JsonAlias("REST_NM")
    private String name;            // 식당 이름

    // 3. 주소
    // [10] @JsonAlias("ADDR"): 통역 설정
    // 상황: 외부에서는 "ADDR" (Address)라고 보냅니다.
    // 동작: 우리 변수 'address'에 연결합니다.
    @JsonAlias("ADDR")
    private String address;         // 주소

    // 4. 상세 주소
    // [11] @JsonAlias("DADDR"): 통역 설정
    // 상황: 외부에서는 "DADDR" (Detail Address)라고 보냅니다.
    // 동작: 우리 변수 'addressDetail'에 연결합니다.
    @JsonAlias("DADDR")
    private String addressDetail;   // 상세 주소

    // 5. 전화번호
    // [12] @JsonAlias("TELNO"): 통역 설정
    // 상황: 외부에서는 "TELNO" (Telephone Number)라고 보냅니다.
    // 동작: 우리 변수 'phone'에 연결합니다.
    @JsonAlias("TELNO")
    private String phone;           // 전화번호

    // 6. 영업 시간
    // [13] @JsonAlias("OPEN_HR_INFO"): 통역 설정
    // 상황: 외부에서는 "OPEN_HR_INFO" (Open Hour Info)라고 보냅니다.
    // 동작: 우리 변수 'openTime'에 연결합니다.
    @JsonAlias("OPEN_HR_INFO")
    private String openTime;        // 영업 시간

    // 7. 식당 분류
    // [14] @JsonAlias("TOB_INFO"): 통역 설정
    // 상황: 외부에서는 "TOB_INFO" (Type of Business Info)라고 보냅니다.
    // 동작: 우리 변수 'restCategory'에 연결합니다. (예: 한식, 중식, 일식)
    @JsonAlias("TOB_INFO")
    private String restCategory;    // 식당 분류

    // 8. 대표 메뉴
    // [15] @JsonAlias("RPRS_MENU_NM"): 통역 설정
    // 상황: 외부에서는 "RPRS_MENU_NM" (Representative Menu Name)이라고 보냅니다.
    // 동작: 우리 변수 'bestMenu'에 연결합니다. (예: 튀김소보로)
    @JsonAlias("RPRS_MENU_NM")
    private String bestMenu;        // 대표 메뉴

    // 9. 전체 메뉴 리스트
    // [16] @JsonAlias("MENU_KORN_NM"): 통역 설정
    // 상황: 외부에서는 "MENU_KORN_NM" (Menu Korean Name)이라고 보냅니다.
    // 타입: 메뉴가 하나가 아니라 여러 개일 수 있어서 List<String>으로 받습니다.
    // 동작: JSON 배열 ["김치찌개", "된장찌개"] 같은 게 들어오면 리스트로 변환해줍니다.
    @JsonAlias("MENU_KORN_NM")
    private List<String> menu;      // 전체 메뉴 리스트

    // 10. 메뉴 상세 정보
    // [17] @JsonAlias("MENU_KORN_ADD_INFO"): 통역 설정
    // 상황: 메뉴에 대한 추가 설명(맵기, 재료 등)을 받습니다.
    @JsonAlias("MENU_KORN_ADD_INFO")
    private List<String> menuDetail; // 메뉴 상세 정보

    // 11. 가격 정보
    // [18] @JsonAlias("MENU_AMT"): 통역 설정
    // 상황: 외부에서는 "MENU_AMT" (Menu Amount)라고 보냅니다.
    // 타입: List<String> (숫자지만 "10,000원"처럼 문자가 섞여 올 수도 있어서 안전하게 String으로 받음)
    @JsonAlias("MENU_AMT")
    private List<String> price;     // 가격 정보

    // 12. 외부 링크
    // [19] @JsonAlias("SD_URL"): 통역 설정
    // 상황: 외부에서는 "SD_URL" (Site Domain URL?)이라고 보냅니다.
    // 동작: 식당 홈페이지나 지도 링크를 'url' 변수에 넣습니다.
    @JsonAlias("SD_URL")
    private String url;             // 식당 관련 외부 링크 (네이버 지도 등)

    // 13. 이미지 경로
    // [20] @JsonAlias("REST_IMAGE"): 통역 설정
    // 특이사항: 보통 공공데이터에는 이미지가 잘 없습니다.
    // 이 필드는 우리가 나중에 크롤링해서 이미지를 구한 뒤, JSON으로 내보내거나 할 때 쓸 필드입니다.
    // 만약 외부 API에서 "REST_IMAGE"라는 키로 이미지를 준다면 여기 매핑되겠지만, 보통은 비어있다가 우리가 채워 넣습니다.
    @JsonAlias("REST_IMAGE")
    private String imagePath;       // 이미지 파일 경로
}

//
//        외부 요청: 우리 서버가 대전시 공공데이터 서버에 "맛집 리스트 주세요!" 하고 요청을 보냅니다.
//
//        외부 응답 (JSON): 대전시 서버가 응답을 줍니다. 그런데 키값이 이상합니다.
//
//        { "REST_NM": "성심당", "ADDR": "대전 중구...", "TELNO": "042..." }
//
//        통역 (Mapping):
//
//        스프링(Jackson 라이브러리)이 이 JSON을 받아서 RestaurantDto를 봅니다.
//
//        "어? REST_NM이 왔네? 여기 @JsonAlias("REST_NM")이 붙은 name 변수에 넣으면 되겠다!"
//
//        이렇게 알아서 척척 데이터를 연결해줍니다.
//
//        사용:
//
//        이제 우리 자바 코드에서는 복잡한 REST_NM 대신 편하게 dto.getName()만 부르면 "성심당"이 나옵니다.