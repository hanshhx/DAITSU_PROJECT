// 1. [지시어] 클라이언트 컴포넌트 선언
"use client";

// 2. [Imports]
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Cookies from "js-cookie";
import { authService } from "@/api/services";

// 4. [Custom Hook Definition]
export const useAuth = () => {
  const router = useRouter();

  // 6. [Function] 일반 로그인
  const login = async (formData: any) => {
    try {
      const response = await authService.login(formData);
      const token = response.data.token || response.data.accessToken;

      if (token) {
        Cookies.set("token", token, { expires: 7, path: "/" });
      }

      window.location.href = "/";
    } catch (error: any) {
      alert(error.response?.data?.message || "로그인에 실패했습니다.");
    }
  };

  // 12. [Function] 로그아웃
  const logout = () => {
    Cookies.remove("token", { path: "/" });
    Cookies.remove("accessToken", { path: "/" });
    window.location.href = "/";
  };

  // 16. [Object] 소셜 로그인
  const socialLogin = {
    // 17. [Naver] 네이버 로그인 함수
    naver: () => {
      const naverClientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID;
      
      console.log("🔹 네이버 Client ID 확인:", naverClientId);

      if (!naverClientId) {
        alert("네이버 로그인 키(NEXT_PUBLIC_NAVER_CLIENT_ID)가 설정되지 않았습니다.");
        return;
      }

      // 네이버도 헷갈리지 않게 고정 주소로 변경 (선택사항이지만 권장)
      const redirectUri = "http://localhost/sign-in/naver/callback";
      const state = "false"; 

      const naverUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${naverClientId}&redirect_uri=${encodeURIComponent(redirectUri)}&state=${state}`;
      
      window.location.href = naverUrl;
    },

    // 18. [Kakao] 카카오 로그인 함수
    kakao: () => {
      // 로그인용 REST API Key 사용
      const kakaoClientId = process.env.NEXT_PUBLIC_KAKAO_REST_KEY;
      
      console.log("🔸 카카오 REST API Key 확인:", kakaoClientId);

      if (!kakaoClientId) {
        alert("카카오 로그인 키(NEXT_PUBLIC_KAKAO_REST_KEY)가 설정되지 않았습니다.");
        return;
      }

      // ▼▼▼ [핵심 수정] 변수 대신 주소를 직접 입력하여 KOE006 원천 차단 ▼▼▼
      // 주의: 카카오 개발자 센터 > Redirect URI 설정에도 이 주소가 있어야 합니다.
      const redirectUri = "http://localhost/sign-in/kakao/callback";
      
      console.log("🔸 카카오 Redirect URI 요청값:", redirectUri);

      // URL 생성
      const kakaoUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${kakaoClientId}&redirect_uri=${redirectUri}&response_type=code`;
      
      window.location.href = kakaoUrl;
    },
  };

  return { login, logout, socialLogin };
};