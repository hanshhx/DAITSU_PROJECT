package com.example.TEAM202507_01.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 값(null)이 없는 필드는 포스트맨/프론트엔드에 보내지 않음
public class MyPageDto {
    // --- 유저 정보 필드 ---
    private String id;        // 고유 식별자 (프론트엔드 key용)
    private String loginId;   // 로그인 아이디 (화면 표시용)
    private String password;
    private String email;
    private String name;
    private String nickname;
    private String birthDate; // YYYY-MM-DD 형식 문자열
    private String gender;

    // --- 게시글/댓글/즐겨찾기 공통 필드 (내 정보 조회 시에는 null이라서 숨겨짐) ---
    private String targetId;  // 글 번호, 즐겨찾기 대상 번호 등
    private String title;     // 글 제목
    private String content;   // 내용
    private String category;  // 카테고리
    private String createdAt; // 작성일
}