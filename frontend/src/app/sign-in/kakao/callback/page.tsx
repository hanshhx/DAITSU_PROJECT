"use client";

import { useEffect, useRef, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Cookies from "js-cookie";
import { authService } from "@/api/services";

// ==================================================================
// [Sub Component] 로딩 화면 UI (따로 컴포넌트 안 만들어도 되게 내장함)
// ==================================================================
function LoadingSpinner({ text }: { text: string }) {
  return (
    <div className="h-screen flex flex-col items-center justify-center bg-white">
      <div className="w-12 h-12 border-4 border-green-200 border-t-green-600 rounded-full animate-spin mb-4"></div>
      <p className="text-slate-500 font-bold animate-pulse">{text}</p>
    </div>
  );
}

// ==================================================================
// [Component 1] 실제 로그인 로직 담당
// ==================================================================
function KakaoCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  
  // React 18 useEffect 중복 실행 방지용
  const isRun = useRef(false);

  useEffect(() => {
    // 1. 코드가 없거나 이미 실행됐다면 스킵
    if (!code || isRun.current) return;

    // 2. 실행 깃발 꽂기
    isRun.current = true;

    const loginProcess = async () => {
      try {
        console.log("🔹 카카오 인증 코드 확인:", code);

        // 3. 백엔드에 로그인 요청 (authService.socialLogin 사용)
        // 주의: 백엔드가 리다이렉트 URI를 요구하면 두 번째 인자로 넘겨줘야 할 수도 있음
        // 지금은 코드만 넘기는 방식으로 진행
        const response = await authService.socialLogin("kakao", code);
        
        // 4. 응답에서 토큰 추출
        const token = response.data.token || response.data.accessToken;

        if (token) {
          console.log("✅ 카카오 로그인 성공! 토큰 저장 중...");
          
          // 5. 쿠키 저장 (1일 유지)
          Cookies.set("token", token, { expires: 1, path: "/" });

          // 6. 메인으로 이동 (새로고침 효과를 위해 window.location 사용)
          window.location.href = "/";
        } else {
          throw new Error("토큰이 없습니다.");
        }

      } catch (err: any) {
        console.error("🚨 로그인 실패:", err);
        
        // 에러 메시지 추출
        const errBody = err.response?.data;
        const errorMessage = typeof errBody === "object" 
          ? JSON.stringify(errBody) 
          : errBody || "로그인 처리 중 오류가 발생했습니다.";

        alert(`로그인 실패: ${errorMessage}`);
        router.push("/sign-in");
      }
    };

    loginProcess();
  }, [code, router]);

  return <LoadingSpinner text="카카오 로그인 처리 중입니다..." />;
}

// ==================================================================
// [Component 2] 메인 페이지 (Suspense 적용)
// ==================================================================
export default function Page() {
  return (
    // useSearchParams를 사용하는 컴포넌트는 반드시 Suspense로 감싸야 에러가 안 납니다.
    <Suspense fallback={<LoadingSpinner text="로딩 중..." />}>
      <KakaoCallbackContent />
    </Suspense>
  );
}