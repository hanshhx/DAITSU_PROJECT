# DAITSHU (다잇슈) - 대전 로컬 정보 & AI 서비스

대전 지역 맛집/병원/관광지 정보 제공 및 AI 기반 이미지 분석(음식/증상) 기능을 제공하는 풀스택 프로젝트

 **1. 기술 스택 (Tech Stack)**

| 구분       | 기술 스택                       | 접속 포트 |
| ---------- | ----------------------------- | -------- |
| Frontend   | Next.js 14, TypeScript, Tailwind CSS | 3000     |
| Backend    | Spring Boot 3, JPA, MyBatis      | 8080 (내부) |
| AI Server  | Python Flask, TensorFlow/Keras   | 5000 (내부) |
| Database   | Oracle DB 21c (XE)              | 1521     |
| Infra      | Docker, Docker Compose, Nginx      | 80 (메인)  |

 **2. [필수] 실행 전 코드 수정 가이드 (Refactoring)**

 코드를 내려받은 후, 실행하기 전에 아래 '하드코딩'된 부분들을 반드시 환경변수로 교체

**2-1. Frontend (Next.js) 수정**

`src/api/axios.ts` 또는 `SearchBar.tsx` 등에서 IP 주소가 직접 적혀있는 부분을 찾아 수정

 잘못된 예 (하드코딩):
 
javascript
// 이렇게 IP가 코드에 박혀있으면 배포 시 접속이 안 됨
const response = await axios.post('http://192.168.0.134:5000/predict', formData);


올바른 예 (환경변수 사용):

javascript
// .env에서 주소를 불러오도록 수정
const aiUrl = process.env.NEXT_PUBLIC_AI_URL || 'http://localhost:5000';
const response = await axios.post(`${aiUrl}/predict`, formData);


 **3. 환경 설정 (.env)**

# ==========================================
# 1. AI & External API Keys
# ==========================================
GEMINI_API_KEY=여기에_키_입력
GOOGLE_PERSPECTIVE_KEY=여기에_키_입력

# ==========================================
# 2. Database (Oracle)
# ==========================================
# Docker 내부 통신용
DB_URL=jdbc:oracle:thin:@//host.docker.internal:1521/xe
DB_USERNAME=아이디
DB_PASSWORD=비밀번호

# ==========================================
# 3. OAuth (Social Login)
# ==========================================
NAVER_CLIENT_ID=네이버_클라이언트_ID
NAVER_CLIENT_SECRET=네이버_시크릿
KAKAO_CLIENT_ID=카카오_클라이언트_ID

# 프론트엔드 노출용 (Next.js)
NEXT_PUBLIC_NAVER_CLIENT_ID=네이버_클라이언트_ID
NEXT_PUBLIC_KAKAO_REST_KEY=카카오_REST_키
NEXT_PUBLIC_KAKAO_MAP_KEY=카카오_지도_JS_키

# ==========================================
# 4. Server URLs (중요!)
# ==========================================
# [프론트 -> 백엔드] Nginx(80포트)를 통해 API 호출 (502 에러 방지)
NEXT_PUBLIC_API_URL=http://localhost/api
# [프론트 -> AI] Nginx(80포트)를 통해 AI 호출 (네트워크 에러 방지)
NEXT_PUBLIC_AI_URL=http://localhost/ai
# [백엔드 -> AI] 도커 내부망 통신용
AI_BASE_URL=http://flask-ai-server:5000

# 서버 내부 설정
SERVER_ADDRESS=0.0.0.0
SERVER_PORT=5000
MODEL_PATH=/app/model/mat.keras
IMG_PATH=/app/temp_img/

🚀 **4. 실행 방법 (How to Run)**

Docker가 설치되어 있어야 함 터미널을 열고 아래 명령어를 순서대로 입력.

**4-1. 전체 서비스 실행**


# 캐시 없이 깨끗하게 새로 빌드해서 실행
docker-compose up -d --build

참고: 오라클 DB 이미지가 커서 처음 실행 시 5~10분 정도 걸릴 수 있음

**4-2. 특정 서비스만 재시작**

코드를 수정했다면 전체를 껐다 켤 필요 없이 해당 서비스만 재시작


**4-3. 종료**

bash
docker-compose down
```
