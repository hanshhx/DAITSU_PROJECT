package com.example.TEAM202507_01.menus.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawledDataDto {
    private String companyName; // 회사명
    private String address;     // 주소
    private String phone;       // 전화번호
    private String title;       // 공고 제목
    private String url;         // 공고 링크(내용)
    private String career;      // 경력
    private String education;   // 학력
    private LocalDate deadline; // 마감일
}