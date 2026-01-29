package com.example.TEAM202507_01.menus.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDto {
    private Long id; //기업의 고유 등록 번호
    private Long typeId; //기업 형태 코드(분류번호)
    private String typeName; //기업 형태 이름
    private String name;// 회사 이름
    private String address; //회사 주소
    private String phone; //회사 대표 전화번호
}
