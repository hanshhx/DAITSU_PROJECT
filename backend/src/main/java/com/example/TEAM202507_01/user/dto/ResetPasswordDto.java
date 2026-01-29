package com.example.TEAM202507_01.user.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String loginId;
    private String name;
    private String email;
}
