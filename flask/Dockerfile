# 1. 가벼운 파이썬 이미지 사용
FROM python:3.9-slim

# 2. 작업 폴더 설정
WORKDIR /app

# [추가] AI 연산 실행 시 필요한 시스템 라이브러리(libgomp1)를 설치합니다.
# slim 이미지에는 이 파일이 없어 TensorFlow 실행 시 에러가 날 수 있습니다.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 3. 라이브러리 목록 복사 및 설치
# (캐싱 효과를 위해 소스 복사 전에 수행)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 소스 코드 전체 복사
# 현재 폴더의 모든 내용(mat_server.py, model 폴더 등)을 컨테이너 내부로 복사합니다.
COPY . .

# [추가] mat_server.py가 이미지를 저장할 폴더를 미리 생성하고 권한을 부여합니다.
RUN mkdir -p /app/temp_img && chmod 777 /app/temp_img

# 5. Flask 기본 포트 노출
EXPOSE 5000

# [추가] 파이썬 로그가 버퍼링 없이 즉시 출력되도록 설정하여 디버깅을 돕습니다.
ENV PYTHONUNBUFFERED=1

# 6. 실행 명령어 (mat_server.py 실행)
CMD ["python", "mat_server.py"]