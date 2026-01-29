import os

# [추가] TensorFlow 로그 레벨 설정 (import tensorflow 전에 실행해야 합니다)
# 0: 모든 로그, 1: INFO 제외, 2: WARNING 제외, 3: ERROR 로그만 출력
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 
# [추가] oneDNN 최적화 관련 경고 숨기기
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

from flask import Flask, request, jsonify
import numpy as np
import tensorflow as tf
from flask_cors import CORS
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()

# 설정값 로드
server = os.getenv('SERVER_ADDRESS', '0.0.0.0')
port = int(os.getenv('SERVER_PORT', 5000))
model_path = os.getenv('MODEL_PATH', '/app/model/mat.keras')
img_path = os.getenv('IMG_PATH', '/app/temp_img/')

# 1. 모델 로드 섹션
print(f"🚀 AI 모델 로딩 중... 경로: {model_path}")
try:
    if os.path.exists(model_path):
        model = tf.keras.models.load_model(model_path)
        print("✅ AI 모델 로딩 완료! 서버가 준비되었습니다.")
    else:
        print(f"❌ 에러: 모델 파일을 찾을 수 없습니다: {model_path}")
        model = None
except Exception as e:
    print(f"❌ 모델 로딩 실패: {e}")
    model = None

def prepare_img(image):
    """이미지를 모델 규격(128x128)으로 가공하고 정규화합니다."""
    # 이미지 크기 조정
    image = tf.keras.preprocessing.image.smart_resize(image, (128, 128))
    # 배열 변환 및 배치 차원 추가 (1, 128, 128, 3)
    image_array = tf.keras.preprocessing.image.img_to_array(image)
    image_array = np.expand_dims(image_array, axis=0)
    # 0~1 사이 값으로 정규화
    image_array = image_array / 255.0
    return image_array

app = Flask(__name__)
CORS(app) # 모든 도메인에 대해 접속 허용

@app.route('/predict', methods=['POST'])
def predict():
    # 모델 로드 상태 확인
    if model is None:
        return jsonify({'error': 'AI 모델이 서버에 로드되지 않았습니다.'}), 500

    # 1. 파일 키 확인
    if 'file' not in request.files:
        return jsonify({'error': '이미지 파일이 전송되지 않았습니다. (Key: file)'}), 400

    file = request.files['file']
    save_path = os.path.join(img_path, file.filename)

    try:
        # [안정성] 저장 폴더가 없으면 생성
        if not os.path.exists(img_path):
            os.makedirs(img_path, exist_ok=True)
            
        # 이미지 저장 및 로드
        file.save(save_path)
        image = tf.keras.utils.load_img(save_path)
        
        # AI 예측 수행
        predictions = model.predict(prepare_img(image), verbose=0) # verbose=0으로 예측 로그도 숨김
        
        # 결과 해석
        result_idx = int(np.argmax(predictions)) # 넘파이 타입을 일반 int로 변환
        probability = f"{np.max(predictions):.2%}" # 확률을 백분율 문자열로 변환

        # 결과 문자열 매핑
        labels = {0: '마들렌', 1: '두쫀쿠', 2: '말차시루'}
        result_str = labels.get(result_idx, '알 수 없는 카테고리')

        # 분석 완료 후 임시 이미지 즉시 삭제
        if os.path.exists(save_path):
            os.remove(save_path)

        print(f"📊 분석 결과: {result_str} ({probability})")
        return jsonify({
            'result': result_str,
            'probability': probability
        }), 200

    except Exception as e:
        # 에러 발생 시에도 파일이 남아있다면 삭제
        if os.path.exists(save_path):
            os.remove(save_path)
        print(f"❌ 예측 중 에러 발생: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """서버 상태 확인용 엔드포인트"""
    return jsonify({'status': 'ok', 'model_loaded': model is not None}), 200

if __name__ == '__main__':
    print(f"=== DAITSU AI SERVER STARTING ON {server}:{port} ===")
    app.run(host=server, port=port)