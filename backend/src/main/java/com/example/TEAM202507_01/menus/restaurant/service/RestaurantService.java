package com.example.TEAM202507_01.menus.restaurant.service;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 서비스' 폴더에 위치한다는 주소입니다.
// 서비스 패키지는 비즈니스 로직의 '목록(Interface)'과 '구현(Class)'이 모여있는 곳입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantBlogDto; // (현재 코드엔 없지만, 블로그 관련 DTO가 필요할 때 씀)
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // 맛집 데이터를 담아 나르는 가방(DTO)
import java.util.List; // 여러 개의 데이터를 담을 리스트 도구

// [3] 인터페이스 선언: "나는 RestaurantService라는 이름의 메뉴판(설계도)입니다."
// class가 아니라 interface입니다. 즉, "기능 이름만 정의할 테니, 실제 동작은 구현체(Impl)가 알아서 해라"는 뜻입니다.
// 구현체 클래스(RestaurantServiceImpl)는 반드시 이 인터페이스를 implements 해야 합니다.
public interface RestaurantService {

    // [4] 전체 목록 조회 기능 정의
    // 기능: 저장된 모든 맛집 리스트를 가져와야 한다.
    // 반환값: List<RestaurantDto> (맛집 데이터 가방이 여러 개 담긴 리스트)
    // 컨트롤러가 "메뉴판 전체 보여줘"라고 할 때 호출합니다.
    List<RestaurantDto> findAll();

    // [5] 상세 조회 기능 정의
    // 기능: 맛집의 고유 번호(id)를 주면, 그 맛집 하나만 딱 집어서 가져와야 한다.
    // 파라미터: Long id (찾고 싶은 식당의 번호)
    // 반환값: RestaurantDto (찾은 맛집 정보 하나. 없으면 null일 수 있음)
    RestaurantDto findById(Long id);

    // [6] 맛집 등록 및 수정 기능 정의
    // 기능: 맛집 정보가 담긴 가방(dto)을 받아서 저장하거나 수정해야 한다.
    // 파라미터: RestaurantDto restaurant (저장할 내용이 담긴 상자)
    // 반환값: RestaurantDto (저장이 완료된 후의 최신 데이터 상태를 돌려줌)
    // 보통 ID가 없으면 '신규 등록', ID가 있으면 '수정'으로 처리하도록 구현합니다.
    RestaurantDto save(RestaurantDto restaurant);

    // [7] 삭제 기능 정의
    // 기능: 식당 번호(id)를 주면 해당 식당을 삭제해야 한다.
    // 파라미터: Long id (지울 식당 번호)
    // 반환값: void (삭제하고 끝이니까 돌려줄 값이 없음)
    void delete(Long id);
}

//
//        약속(Contract):
//
//        팀장님이 개발자에게 지시합니다. "맛집 관리 기능을 만들 건데, 무조건 조회, 상세조회, 저장, 삭제 이 4가지는 꼭 있어야 해!"
//
//        이 인터페이스가 바로 그 **지시서(약속)**입니다.
//
//        컨트롤러의 시선:
//
//        RestaurantController는 복잡한 내부 로직(DB 연결, 트랜잭션 등)은 몰라도 됩니다.
//
//        그저 이 인터페이스를 보고 "아, findAll()을 부르면 맛집 리스트를 주는구나" 하고 믿고 호출합니다.
//
//        유연성:
//
//        만약 나중에 맛집 데이터를 DB가 아니라 엑셀 파일에서 읽어오도록 바꿔야 한다면?
//
//        이 인터페이스는 그대로 두고, 뒤에 연결된 구현체(ServiceImpl)만 갈아 끼우면 됩니다. 컨트롤러 코드는 수정할 필요가 없죠.