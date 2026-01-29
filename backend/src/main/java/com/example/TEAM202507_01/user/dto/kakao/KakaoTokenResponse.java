package com.example.TEAM202507_01.user.dto.kakao;

import lombok.Data;

@Data
public class KakaoTokenResponse {
    // access_token 등등이 넘어오지만 우리는 access_token만 필요함
    @com.fasterxml.jackson.annotation.JsonProperty("access_token")
    private String accessToken;
}