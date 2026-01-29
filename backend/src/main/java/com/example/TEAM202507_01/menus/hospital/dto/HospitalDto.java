package com.example.TEAM202507_01.menus.hospital.dto;
// [1] 패키지 선언: 이 파일이 컴퓨터 어느 폴더에 살고 있는지 주소를 적어둔 것입니다.
// 여기서는 '병원(hospital) 메뉴' 관련 파일 중 '데이터 가방(dto)'들을 모아둔 곳에 위치합니다.

// [2] 임포트(Import): 이 클래스를 만들 때 필요한 도구(라이브러리)들을 가져옵니다.
import lombok.AllArgsConstructor; // 모든 필드 값을 한 번에 채우는 생성자를 만드는 도구
import lombok.Builder;            // 객체를 조립하듯 예쁘게 만들 수 있게 해주는 도구 (빌더 패턴)
import lombok.Data;               // Getter, Setter, toString 등 필수 기능 자동 생성 도구
import lombok.NoArgsConstructor;  // 아무 값도 없는 빈 깡통 객체를 만드는 생성자 도구

// [참고] 아래 두 줄은 검색엔진(Elasticsearch) 기능을 쓰려고 가져온 건데, 현재 코드 필드에는 적용이 안 되어 있네요.
// 나중에 검색 기능 고도화할 때 쓰려고 미리 가져온 것 같습니다.
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime; // 날짜와 시간(년-월-일 시:분:초)을 다루기 위한 자바 표준 도구

// [3] 어노테이션(Annotation): 이 클래스에 마법 같은 능력을 부여합니다.
@Data
// 의미: "롬복아, 아래 변수들의 Getter(꺼내기), Setter(넣기), toString(출력하기) 코드를 네가 알아서 다 만들어줘."
// 효과: 코드가 엄청 짧아지고 깔끔해집니다.

@Builder
// 의미: "객체를 만들 때 순서 헷갈리지 않게 이름표 붙여서 만들게 해줘."
// 사용법: HospitalDto.builder().name("대전병원").tel("042-123-4567").build(); 처럼 직관적으로 쓸 수 있습니다.

@NoArgsConstructor
// 의미: "파라미터 없는 기본 생성자(public HospitalDto() {})를 만들어줘."
// 이유: DB에서 데이터를 가져오거나 JSON을 변환할 때, 일단 빈 객체를 먼저 만들기 때문에 이게 꼭 필요합니다.

@AllArgsConstructor
// 의미: "모든 필드를 다 채우는 생성자를 만들어줘."
// 이유: 위의 @Builder가 작동하려면 전체 생성자가 반드시 필요하기 때문입니다.

// [4] 클래스 선언: "이제부터 HospitalDto라는 이름의 데이터 가방을 정의할게."
public class HospitalDto {

    // [5] 필드(Field): 가방 안에 들어갈 내용물(데이터)들을 정의합니다.

    private Long id;
    // 의미: 병원 고유 번호 (주민등록번호 같은 것)
    // 타입: Long (아주 큰 정수)
    // 이유: 데이터가 수십억 개가 넘어가면 int(약 21억까지 가능)로는 부족할 수 있어서 더 큰 Long을 씁니다.

    private String category;
    // 의미: 병원 종류 분류
    // 예시: "종합병원", "치과", "한의원", "내과" 등

    private String name;
    // 의미: 병원 이름
    // 예시: "대전 한국병원", "둔산 내과"

    private String treatCategory;
    // 의미: 진료 과목 (소분류)
    // 예시: "감기", "골절", "피부과" 등 사용자가 검색하기 쉬운 태그 느낌

    private String address;
    // 의미: 병원 상세 주소
    // 예시: "대전광역시 동구 동서대로 1234"

    private String tel;
    // 의미: 전화번호
    // 타입: String (문자열)
    // 이유: 전화번호는 숫자지만 더하기 빼기 할 일이 없죠?
    // 그리고 "010-1234-5678"처럼 하이픈(-)도 들어가야 하고, 맨 앞이 0으로 시작하기 때문에 문자로 저장해야 합니다.

    private LocalDateTime editDate;
    // 의미: 정보 수정일
    // 타입: LocalDateTime (날짜 + 시간)
    // 용도: 병원 정보가 최신인지, 언제 업데이트됐는지 보여줄 때 사용합니다.

    private Double averageRating;
    // 의미: 평균 별점
    // 타입: Double (소수점 숫자)
    // 이유: 별점 평균은 "4.5", "3.8" 처럼 소수점이 나오기 때문에 정수(Integer)가 아닌 실수(Double)를 씁니다.
    // 특징: 이 값은 DB 병원 테이블에 원래 있는 게 아니라, 리뷰 테이블을 계산해서 서비스(Service)가 채워 넣을 확률이 높습니다.

    private Integer reviewCount;
    // 의미: 총 리뷰 개수
    // 타입: Integer (정수)
    // 이유: 리뷰 개수는 "1개", "100개" 처럼 딱 떨어지는 정수니까요.
    // 용도: "리뷰 1,234개" 처럼 인기도를 보여줄 때 사용합니다.
}

//
//        DB 조회: 서버가 DB에서 병원 데이터를 꺼냅니다. (이때는 아직 날것의 데이터입니다.)
//
//        데이터 포장 (HospitalDto):
//
//        가져온 데이터 중, 화면에 보여줄 것들만 골라서 이 HospitalDto 상자에 담습니다.
//
//        이때, averageRating(평점)이나 reviewCount(리뷰 수) 같이 DB 병원 테이블에는 없지만 계산해서 보여줘야 하는 값들도 이 상자에 같이 담습니다.
//
//        배송 (Response):
//
//        컨트롤러가 이 상자를 JSON 형식({ "name": "대전병원", "rating": 4.5 ... })으로 바꿔서 사용자에게 보냅니다.
//
//        화면 표시:
//
//        사용자의 스마트폰 화면에 "대전병원 (⭐ 4.5)"라고 예쁘게 뜹니다.