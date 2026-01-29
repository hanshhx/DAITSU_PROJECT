package com.example.TEAM202507_01.menus.tour.dto;

import lombok.*;

// [어노테이션 설명]
// @Getter, @Setter: 모든 필드의 값을 꺼내거나 넣는 메서드 자동 생성.
// @NoArgsConstructor: 파라미터 없는 기본 생성자 생성 (JPA/JSON 변환 시 필수).
// @AllArgsConstructor: 모든 필드를 넣는 생성자 생성.
// @Builder: 객체 생성 시 TourDto.builder().name("...").build() 처럼 깔끔하게 만들 수 있게 함.
// @ToString: 객체 정보를 문자열로 출력할 때 사용.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TourDto {
    private Long id;            // 관광지 고유 번호 (PK) - DB의 tourspotZip 컬럼과 매칭 예상
    private String name;        // 관광지 이름
    private String address;     // 지번 주소
    private String phone;       // 전화번호
    private String description; // 관광지 설명 (개요)
    private String image; // 이미지 주소
}









