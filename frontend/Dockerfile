# 1. Node.js 20 버전 사용 (Next.js 최신 버전 호환)
FROM node:20-alpine AS base

# 2. 의존성 설치 단계
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci

# 3. 빌드 단계
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# ▼▼▼ [여기가 핵심!] docker-compose에서 보낸 변수들을 다 받아야 합니다 ▼▼▼
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_NAVER_CLIENT_ID
ARG NEXT_PUBLIC_KAKAO_MAP_KEY
# [추가됨] 로그인용 REST 키도 받아야 함!
ARG NEXT_PUBLIC_KAKAO_REST_KEY 

# 받은 변수를 환경 변수로 등록
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_NAVER_CLIENT_ID=$NEXT_PUBLIC_NAVER_CLIENT_ID
ENV NEXT_PUBLIC_KAKAO_MAP_KEY=$NEXT_PUBLIC_KAKAO_MAP_KEY
# [추가됨] 로그인용 REST 키 등록!
ENV NEXT_PUBLIC_KAKAO_REST_KEY=$NEXT_PUBLIC_KAKAO_REST_KEY
# ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

# Next.js 빌드 실행
RUN npm run build

# 4. 실행 단계
FROM base AS runner
WORKDIR /app
ENV NODE_ENV=production

# 보안을 위해 nextjs 유저 생성
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

# 빌드된 결과물 복사
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]