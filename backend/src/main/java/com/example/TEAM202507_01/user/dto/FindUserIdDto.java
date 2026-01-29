package com.example.TEAM202507_01.user.dto;


import lombok.Data;

@Data

public class FindUserIdDto {
    private String name;
    private String email;
    private String token;
}
