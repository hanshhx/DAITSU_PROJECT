# ============================================
# 1. 빌드 단계 (Builder Stage)
# ============================================
# Gradle 공식 이미지를 사용하여 빌드합니다.
FROM gradle:8.5-jdk21 AS builder

# 작업 폴더 설정
WORKDIR /app

# 소스 코드 복사 (의존성 캐싱을 위해 build.gradle 먼저 복사하는 게 좋지만, 지금은 간단히 전체 복사)
COPY . .

# 빌드 실행 (테스트 제외, 데몬 미사용)
# 리눅스/윈도우 줄바꿈 호환성을 위해 gradle 명령어를 직접 사용
RUN gradle clean build -x test --no-daemon

# ============================================
# 2. 실행 단계 (Runtime Stage)
# ============================================
# OpenJDK 대신 가장 안정적인 Eclipse Temurin 이미지를 사용합니다. (확실하게 존재하는 이미지)
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드 결과물(.jar)을 실행 컨테이너로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]