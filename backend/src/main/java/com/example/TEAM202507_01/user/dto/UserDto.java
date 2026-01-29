package com.example.TEAM202507_01.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    private String id;            // 회원 고유 번호 (UUID, PK) ID
    private String loginId;     // 로그인 아이디 LOGIN_ID

    @JsonIgnore
    private String password;    // 비밀번호 (암호화 필요) PASSWORD

    private String email;       // 이메일 EMAIL
    private String name;        // 이름 name
    private String nickname;    // 닉네임 nickName
    private LocalDate birthDate; // 생년월일 birth
    private String gender;      // 성별 (M, F) sex
    private LocalDateTime createdAt; // 생성일 createdAt
    private List<String> authorities;
}









