package com.example.TEAM202507_01.menus.restaurant.service;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: DTO, Mapper, 스프링 어노테이션, 리스트 도구 등을 가져옵니다.
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // 맛집 데이터 가방
import com.example.TEAM202507_01.menus.restaurant.repository.RestaurantMapper; // DB 관리자
import lombok.RequiredArgsConstructor; // 생성자 자동 생성
import org.springframework.stereotype.Service; // 서비스 빈 등록
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리

import java.util.ArrayList; // 수정 가능한 리스트
import java.util.List; // 리스트 인터페이스

@Service
// [3] 어노테이션(@Service): "스프링아, 내가 진짜 비즈니스 로직을 담당하는 일꾼(Bean)이야."

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): final이 붙은 필드(restaurantMapper)를 초기화하는 생성자를 자동으로 만듭니다.

@Transactional
// [5] 어노테이션(@Transactional): 이 클래스의 모든 메서드에 대해 "도중에 에러 나면 자동 취소(Rollback)" 기능을 겁니다.
public class RestaurantServiceImpl implements RestaurantService {

    // [6] 의존성 주입: DB 작업을 대신 해줄 매퍼(창고지기)를 데려옵니다.
    private final RestaurantMapper restaurantMapper;

    // =========================================================
    // 1. 전체 목록 조회
    // =========================================================
    @Override
    @Transactional(readOnly = true) // [7] 읽기 전용 모드: 조회 속도 향상 (변경 감지 안 함)
    public List<RestaurantDto> findAll() {
        // [8] 매퍼에게 시켜서 DB에 있는 모든 식당 리스트를 가져옵니다.
        List<RestaurantDto> fixedList = restaurantMapper.findAll();

        // [9] 리스트 순회: 가져온 식당들을 하나씩 꺼내서 검사합니다.
        for (RestaurantDto restaurantDto : fixedList) {
            // [10] 데이터 보정: 메뉴가 비어있으면 채워넣는 메서드(아래 정의됨)를 호출합니다.
            fillMissingData(restaurantDto);
        }

        // [11] 메뉴까지 꽉 채워진 리스트를 반환합니다.
        return fixedList;
    }

    // =========================================================
    // 2. 상세 조회
    // =========================================================
    @Override
    @Transactional(readOnly = true) // 읽기 전용
    public RestaurantDto findById(Long id) {
        // [12] 매퍼에게 ID로 식당 하나를 찾아오라고 시킵니다.
        RestaurantDto restaurantDto = restaurantMapper.findById(id);

        // [13] 예외 처리: 만약 없는 ID(예: 9999)를 조회하면 에러를 냅니다.
        if (restaurantDto == null) {
            throw new RuntimeException("해당 맛집을 찾을 수 없습니다. ID: " + id);
        }

        // [14] 데이터 보정: 상세 조회 때도 메뉴가 없으면 자동으로 채워줍니다.
        fillMissingData(restaurantDto);

        return restaurantDto;
    }

    // =========================================================
    // 3. 저장 및 수정 (Upsert)
    // =========================================================
    @Override
    public RestaurantDto save(RestaurantDto restaurant) {
        // [15] ID 체크: ID가 없으면 '새 식당'이고, 있으면 '기존 식당'입니다.
        if (restaurant.getId() == null) {
            // ID 없음 -> 신규 등록 (INSERT 쿼리 실행)
            restaurantMapper.save(restaurant);
        } else {
            // ID 있음 -> 정보 수정 (UPDATE 쿼리 실행)
            restaurantMapper.update(restaurant);
        }
        // 저장된 객체를 그대로 돌려줍니다.
        return restaurant;
    }

    // =========================================================
    // 4. 삭제
    // =========================================================
    @Override
    public void delete(Long id) {
        // [16] 매퍼에게 해당 ID 식당을 지우라고 명령합니다.
        restaurantMapper.delete(id);
    }

    // =========================================================
    // 🔥 [핵심 로직] 누락된 데이터 자동 채우기
    // =========================================================
    // 기능: 식당의 메뉴나 대표 메뉴가 비어있을 때, 식당 이름과 카테고리를 분석해서 그럴싸한 메뉴를 채워 넣습니다.
    private void fillMissingData(RestaurantDto restaurantDto) {

        // ------------------------------------------
        // (1) 메뉴 리스트 채우기
        // ------------------------------------------
        // [17] 조건: 메뉴 리스트가 아예 없거나(null) 비어있을 때만 실행합니다.
        if (restaurantDto.getMenu() == null || restaurantDto.getMenu().isEmpty()) {

            // [18] 중요: 식당마다 메뉴가 다르니까, 매번 새로운 리스트(ArrayList)를 만들어야 합니다.
            // (이걸 밖에서 만들고 재사용하면 모든 식당 메뉴가 똑같아지는 참사가 일어납니다.)
            List<String> menuList = new ArrayList<>();

            // 판단 근거가 될 카테고리와 식당 이름을 가져옵니다.
            String category = restaurantDto.getRestCategory();
            String name = restaurantDto.getName();

            // [19] 카테고리가 null이면 분석할 수 없으니 체크합니다.
            if (category != null) {
                // --- [한식] 분석 ---
                if (category.equals("한식")) {
                    // 이름에 '족발'이 있으면 -> 족발, 수육 추가
                    if (name.contains("족발")) {
                        menuList.add("족발");
                        menuList.add("수육");
                    }
                    // 이름에 '만두'가 있으면 -> 만두 종류 추가
                    else if (name.contains("만두")) {
                        menuList.add("갈비만두");
                        menuList.add("찐만두");
                        menuList.add("군만두");
                    }
                    // 이름에 '찜'이 있으면 -> 김치찜, 아구찜 추가
                    else if (name.contains("찜")) {
                        menuList.add("김치찜");
                        menuList.add("아구찜");
                    }
                    // 이름에 '찌개'가 있으면 -> 찌개류 추가
                    else if (name.contains("찌개")) {
                        menuList.add("김치찌개");
                        menuList.add("된장찌개");
                    }
                    // 이름에 '고기'가 있으면 -> 구이류 추가
                    else if (name.contains("고기")) {
                        menuList.add("삼겹살");
                        menuList.add("목살");
                    }
                    // 이름에 '국수'가 있으면 -> 국수류 추가
                    else if (name.contains("국수")) {
                        menuList.add("칼국수");
                        menuList.add("비빔국수");
                    }
                    // 이름에 '김밥'이 있으면 -> 김밥류 추가
                    else if (name.contains("김밥")) {
                        menuList.add("김밥");
                        menuList.add("참치김밥");
                        menuList.add("꼬마김밥");
                    }
                    // [20] 한식당 공통 기본 메뉴: 위 조건에 안 걸려도 이건 기본으로 넣어줍니다.
                    menuList.add("국밥");
                    menuList.add("특대국밥");
                    menuList.add("갈비탕");

                }
                // --- [일식] ---
                else if (category.equals("일식")) {
                    menuList.add("돈까스");
                    menuList.add("회");
                    menuList.add("우동");
                }
                // --- [중식] ---
                else if (category.equals("중식")) {
                    menuList.add("짜장면");
                    menuList.add("짬뽕");
                    menuList.add("탕수육");
                }
                // --- [양식] ---
                else if (category.equals("양식")) {
                    menuList.add("파스타");
                    menuList.add("스테이크");
                }
                // --- [분식] ---
                else if (category.equals("분식")) {
                    menuList.add("떡볶이");
                    menuList.add("순대");
                    menuList.add("어묵");
                    menuList.add("김밥");
                }
                // --- [치킨] ---
                else if (category.equals("치킨")) {
                    menuList.add("치킨");
                    menuList.add("양념치킨");
                }
                // --- [카페] ---
                else if (category.equals("카페·디저트")) {
                    menuList.add("아메리카노");
                    menuList.add("카페라떼");
                    menuList.add("바닐라라떼");
                }
            }
            // [21] 이렇게 열심히 만든 가짜(?) 메뉴 리스트를 DTO에 저장합니다.
            // 이제 화면에는 이 메뉴들이 보입니다.
            restaurantDto.setMenu(menuList);
        }

        // ------------------------------------------
        // (2) 대표 메뉴(BestMenu) 채우기
        // ------------------------------------------
        // [22] 조건: 대표 메뉴가 비어있으면 실행합니다.
        // 로직은 위와 비슷합니다. 카테고리와 이름을 보고 가장 그럴싸한 메뉴 하나를 정해줍니다.
        if (restaurantDto.getBestMenu() == null || restaurantDto.getBestMenu().isEmpty()) {
            String category = restaurantDto.getRestCategory();
            String name = restaurantDto.getName();

            if (category != null) {
                if (category.equals("한식")) {
                    if (name.contains("족발")) restaurantDto.setBestMenu("족발");
                    else if (name.contains("만두")) restaurantDto.setBestMenu("갈비만두");
                    else if (name.contains("찜")) restaurantDto.setBestMenu("김치찜");
                    else if (name.contains("찌개")) restaurantDto.setBestMenu("김치찌개");
                    else if (name.contains("고기")) restaurantDto.setBestMenu("삼겹살");
                    else if (name.contains("국수")) restaurantDto.setBestMenu("칼국수");
                    else if (name.contains("김밥")) restaurantDto.setBestMenu("김밥");
                    else restaurantDto.setBestMenu("국밥"); // 한식 기본값
                } else if (category.equals("일식")) {
                    restaurantDto.setBestMenu("돈까스");
                } else if (category.equals("중식")) {
                    restaurantDto.setBestMenu("짜장면");
                } else if (category.equals("양식")) {
                    restaurantDto.setBestMenu("파스타");
                } else if (category.equals("분식")) {
                    restaurantDto.setBestMenu("떡볶이");
                } else if (category.equals("치킨")) {
                    restaurantDto.setBestMenu("치킨");
                } else if (category.equals("카페·디저트")) {
                    restaurantDto.setBestMenu("아메리카노");
                }
            }
        }
    }
}
//
//상황: 사용자가 "한식" 카테고리를 눌렀을 때
//
//조회 (Fetch):
//
//서비스가 매퍼(DB)에게 "모든 식당 다 가져와!"라고 합니다 (findAll).
//
//DB에서 식당 100개를 줍니다. 그런데 "김씨네 족발"이라는 식당에 메뉴 정보가 비어있습니다.
//
//        데이터 보정 (Filling Data):
//
//서비스는 데이터를 바로 사용자에게 주지 않고, **fillMissingData**라는 검수 과정을 거칩니다.
//
//        "어? '김씨네 족발'인데 메뉴가 없네? 이름에 '족발'이 들어가니까 메뉴에 '족발', '수육'을 넣어줘야겠다."
//
//        "어? '홍콩반점'은 중식이네? 메뉴에 '짜장면', '짬뽕'을 넣어주자."
//
//이렇게 코드가 알아서 적절한 메뉴를 채워 넣습니다.
//
//반환 (Return):
//
//이제 메뉴판이 꽉 채워진 데이터를 컨트롤러에게 전달합니다. 사용자는 앱에서 텅 빈 화면 대신 그럴싸한 메뉴 정보를 보게 됩니다.