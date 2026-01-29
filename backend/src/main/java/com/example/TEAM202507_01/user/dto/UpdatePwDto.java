package com.example.TEAM202507_01.user.dto;

import lombok.Data;

@Data
public class UpdatePwDto {
    private String email;
    private String token;
    private String password;
}
