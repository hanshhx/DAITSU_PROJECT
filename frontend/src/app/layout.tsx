// 1. [Import] Next.js에서 제공하는 'Metadata'와 'Viewport' 타입을 가져옵니다.
import type { Metadata, Viewport } from "next";

// 2. [Import] 전역 스타일 시트(CSS)를 불러옵니다.
import "./globals.css";

// 3. [Import] Next.js의 폰트 최적화 기능을 가져옵니다.
import localFont from "next/font/local";

// ✅ [추가] Next.js의 스크립트 최적화 로더를 가져옵니다.
import Script from "next/script";

// 4. [Font Config] 'Pretendard' 가변 폰트를 설정합니다.
const pretendard = localFont({
  src: "./fonts/PretendardVariable.woff2",
  display: "swap",
  weight: "45 920",
  variable: "--font-pretendard",
});

// 5. [Metadata] 사이트의 검색엔진 최적화(SEO) 정보를 설정합니다.
export const metadata: Metadata = {
  title: "다잇슈대전",
  description: "대전 지역 커뮤니티 사이트",
};

// ++ 뷰포트 설정 (모바일 환경에서 화면 고정 및 자동확대 방지)
export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

// 6. [Root Layout] 이 프로젝트의 최상위 부모 컴포넌트입니다.
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className={`${pretendard.variable}`}>
      <body className="antialiased">
        {/* ✅ [핵심 수정] 카카오 지도 스크립트를 여기서 전역으로 딱 한 번만 로드합니다. */}
        {/* 이렇게 하면 페이지를 옮겨 다녀도 중복 로드 에러(Loader called again)가 발생하지 않습니다. */}
        <Script
          src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY || "ed46603fb133bbedb6eb40c5fe4b0278"}&libraries=services,clusterer&autoload=false`}
          strategy="beforeInteractive" // 페이지 상호작용 전에 미리 불러와서 준비시킵니다.
        />
        
        {/* 실제 페이지 내용물 */}
        {children}
      </body>
    </html>
  );
}