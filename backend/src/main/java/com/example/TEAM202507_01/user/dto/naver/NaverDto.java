package com.example.TEAM202507_01.user.dto.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NaverDto {

    @Data
    public static class TokenResponse {
        // ★ 핵심 수정: JSON의 "access_token"을 자바의 "accessToken"에 연결
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private String expiresIn;
    }

    @Data
    public static class UserInfoResponse {
        @JsonProperty("resultcode")
        private String resultCode;

        @JsonProperty("message")
        private String message;

        @JsonProperty("response")
        private Response response;

        @Data
        public static class Response {
            private String id;
            private String email;
            private String name;
            private String nickname;
            private String gender;
            private String birthday;
            private String birthyear;
            private String mobile;
        }
    }
}