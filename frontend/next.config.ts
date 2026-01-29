import type { NextConfig } from "next";

const nextConfig: NextConfig = {

  /* config options here */
  output: 'standalone',
  images: {
    unoptimized: true, 
  },
  
  // ▼▼▼ 여기에 추가했습니다 ▼▼▼
  env: {
    // 백엔드 API 주소 (Docker 빌드 시점에 환경변수로 주입됨)
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
    
    // AI 서버 주소 (서버 사이드 렌더링 시 Docker 내부 통신용)
    AI_BASE_URL: process.env.AI_BASE_URL,
  },
};

export default nextConfig;