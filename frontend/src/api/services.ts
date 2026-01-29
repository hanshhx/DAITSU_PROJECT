import api from "@/api/axios";

// 1. 인증 관련 서비스 (로그인, 회원가입 - 페이지에서 사용하는 부분)
export const authService = {
  login: (data: any) => api.post("/user/login", data),
  logout: () => api.post("/user/logout"),
  signUp: (data: any) => api.post("/user/join", data),
  
  checkIdDuplicate: (loginId: string) =>
    api.get("/user/check-id", { params: { loginId } }),
  
  sendEmailVerification: (email: string) =>
    api.post("/user/checkemail/get-token", { email }),
  
  verifyEmailCode: (data: { email: string; token: string }) =>
    api.post("/user/checkemail", data),

  // ▼▼▼ [핵심 추가] 이 함수가 없어서 빌드 에러가 났던 것입니다. ▼▼▼
  // 카카오/네이버 등 소셜 로그인을 통합 처리하는 함수
  socialLogin: (provider: string, code: string, state?: string | null) => {
    // 백엔드 API 주소: /auth/kakao 또는 /auth/naver (POST 요청)
    return api.post(`/auth/${provider}`, { 
      code, 
      state // 네이버일 때만 값이 들어감
    });
  },
  // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

  // (기존 함수들도 혹시 모르니 유지)
  kakaoLogin: (code: string) => api.get(`/auth/kakao/callback?code=${code}`),
  naverLogin: (loginData: { code: string; state: string }) =>
    api.post("/auth/naver/login", loginData, {
      headers: {
        "Content-Type": "application/json",
      },
    }),
};

// 2. 유저 관련 서비스 (프로필, 정보 수정 등)
export const userService = {
  getUserInfo: () => api.get("/mypage/info"),
  updateUserInfo: (data: any) => api.put("/mypage/info", data),
  getFavorites: () => api.get("/mypage/favorites"),
  getRole: () => api.get("/admin/status"),
};

// 3. 채용 정보 관련 서비스
export const jobService = {
  getCrawledJobs: (params?: Record<string, string | number>) => {
    const query = params ? new URLSearchParams(params as any).toString() : "";
    return api.get(`/job/crawl${query ? `?${query}` : ""}`);
  },
  applyJob: (data: any) => api.post("/job/apply", data),
};

// 4. 맛집 관련 서비스
export const restaurantService = {
  getRestaurants: () => api.get("/restaurant"),
  getRestaurantDetail: (id: string | number) => api.get(`/restaurant/${id}`),
  toggleFavorite: (id: number) => api.post(`/restaurant/${id}/favorite`),
};

// 5. 여행 코스 관련 서비스
export const tourService = {
  getTourCourses: () => api.get("/tour"),
  getTourDetail: (id: string) => api.get(`/tour/${id}`),
  toggleFavorite: (id: number) => api.post(`/tour/${id}/favorite`),
};

// 6. 병원/지도 관련 서비스
export const hospitalService = {
  getHospitals: () => api.get("/hospital"),
  getHospitalDetail: (id: number) => api.get(`/hospital/${id}`),
  toggleFavorite: (id: number) => api.post(`/hospital/${id}/favorite`),
};

// 7. 게시판 관련 서비스
export const boardService = {
  // 목록 조회
  getBoardPosts: (category: string) => {
    const endpoint =
      category === "free" ? "/community/free" : "/community/notice";
    return api.get(endpoint);
  },

  // 상세 조회
  getPostDetail: (category: string, id: string) => {
    const endpoint =
      category === "free" ? `/community/free/${id}` : `/community/notice/${id}`;
    return api.get(endpoint);
  },

  // 게시글 작성
  createPost: (category: string, data: any) => {
    const endpoint =
      category === "notice" ? "/community/notice" : "/community/free";
    return api.post(endpoint, data);
  },

  // 게시글 삭제
  deletePost: (category: string, id: string | number) => {
    const endpoint =
      category === "free" ? `/community/free/${id}` : `/community/notice/${id}`;
    return api.delete(endpoint);
  },

  // 댓글 관련 로직
  getComments: (postId: string) => api.get(`/community/comments/${postId}`),
  createComment: (data: any) => api.post("/community/comments", data),
  deleteComment: (commentId: number) =>
    api.delete(`/community/comments/${commentId}`),
};