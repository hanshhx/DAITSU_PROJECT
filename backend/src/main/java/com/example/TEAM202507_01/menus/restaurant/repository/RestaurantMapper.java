package com.example.TEAM202507_01.menus.restaurant.repository;
// [1] 패키지 선언: 이 파일이 '맛집 메뉴 > 저장소(repository)' 폴더에 위치한다는 주소입니다.
// Repository 패키지는 DB와 직접 대화하는 파일(Mapper)들이 모여있는 곳입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.restaurant.dto.RestaurantDto; // DB 데이터를 담을 '맛집 데이터 가방'
import org.apache.ibatis.annotations.Mapper; // MyBatis 프레임워크가 제공하는 '이건 매퍼야'라는 표시
import org.apache.ibatis.annotations.Param; // SQL에 파라미터를 여러 개 넘길 때 이름표를 붙여주는 도구
import java.util.List; // 데이터를 여러 개 담을 때 쓰는 리스트 도구

// [3] 어노테이션(@Mapper): MyBatis의 마법 지팡이
// 의미: "스프링아, 이건 그냥 껍데기(인터페이스)지만, 실행될 때 네가 알아서 구현체(실제 작동하는 코드)를 만들어서 DB랑 연결해줘."
// 효과: 개발자가 JDBC 연결 코드를 일일이 짜지 않아도, 메서드 이름과 XML의 SQL ID만 맞추면 DB 연결이 됩니다.
@Mapper
public interface RestaurantMapper {
    // [4] 인터페이스 선언: "나는 RestaurantMapper라는 이름의 DB 관리자입니다."

    // ==========================================
    // 1. 기본 조회 기능
    // ==========================================

    // [5] 전체 조회
    // 기능: DB 테이블(RESTAURANT)에 있는 모든 식당 데이터를 싹 다 가져옵니다.
    // 반환값: List<RestaurantDto> (식당이 여러 개니까 리스트에 담아서 줍니다)
    // 연결된 SQL: SELECT * FROM RESTAURANT
    List<RestaurantDto> findAll();

    // [6] 상세 조회
    // 기능: 식당의 고유 번호(id)를 주면, 그 식당 하나만 콕 집어서 가져옵니다.
    // 용도: 목록에서 '성심당'을 클릭했을 때 상세 페이지를 보여주기 위해 씁니다.
    // 반환값: RestaurantDto (식당 하나. 없으면 null)
    // 연결된 SQL: SELECT * FROM RESTAURANT WHERE ID = #{id}
    RestaurantDto findById(Long id);

    // [7] 이름 검색용 조회 (크롤링 보조용)
    // 기능: ID를 주면 식당 정보(주로 이름)를 가져옵니다.
    // 용도: 크롤러가 "이 ID 식당 이름이 뭐더라?" 하고 확인할 때 씁니다.
    // findById와 비슷해 보이지만, 아마 SQL에서 가져오는 컬럼이 더 적거나 특정 목적에 최적화된 쿼리일 겁니다.
    RestaurantDto findNameById(Long id);

    // ==========================================
    // 2. 데이터 변경 기능 (CUD)
    // ==========================================

    // [8] 식당 등록 (Insert)
    // 기능: 새로운 식당 정보(DTO)를 받아서 DB에 저장합니다.
    // 용도: 관리자가 수동으로 등록하거나, 공공데이터를 동기화할 때 씁니다.
    // 반환값: void (저장하고 끝)
    // 연결된 SQL: INSERT INTO RESTAURANT (...) VALUES (...)
    void save(RestaurantDto restaurant);

    // [9] 식당 정보 수정 (Update)
    // 기능: 기존 식당 정보(DTO)를 받아서 내용을 갱신합니다.
    // 용도: 전화번호가 바뀌었거나 메뉴가 변경되었을 때 씁니다.
    // 연결된 SQL: UPDATE RESTAURANT SET NAME=#{name}... WHERE ID=#{id}
    void update(RestaurantDto restaurant);

    // [10] 식당 삭제 (Delete)
    // 기능: ID를 주면 해당 식당을 DB에서 삭제합니다.
    // 연결된 SQL: DELETE FROM RESTAURANT WHERE ID = #{id}
    void delete(Long id);

    // ==========================================
    // 3. 이미지 크롤링 관련 특수 기능
    // ==========================================

    // [11] 이미지 경로 업데이트
    // 기능: 특정 식당(id)의 이미지 주소(imagePath)만 콕 집어서 수정합니다.
    // @Param 설명: 파라미터가 2개(id, imagePath)라서 헷갈리지 않게 이름표를 붙입니다.
    // XML 파일에서는 #{imageId}, #{imagePath} 라는 이름으로 이 값들을 꺼내 쓸 수 있습니다.
    // 연결된 SQL: UPDATE RESTAURANT SET IMAGE_PATH = #{imagePath} WHERE ID = #{imageId}
    void updateImage(@Param("imageId") Long id, @Param("imagePath") String imagePath);

    // [12] URL이 있는 식당만 조회
    // 기능: 전체 식당 중에 '홈페이지 URL'이 있는 식당들만 골라서 가져옵니다.
    // 용도: 이미지 크롤러가 작동할 때, URL이 없는 식당은 어차피 사진을 못 구하니까 애초에 목록에서 빼버려서 효율을 높이기 위함입니다.
    // 연결된 SQL: SELECT * FROM RESTAURANT WHERE URL IS NOT NULL
    List<RestaurantDto> findAllWithUrl();
}
//
//        맛집 리스트 보기 (findAll):
//
//        사용자가 앱을 켜면 서비스가 findAll()을 호출합니다.
//
//        매퍼는 SELECT * FROM RESTAURANT 쿼리를 날려 모든 식당 정보를 가져옵니다.
//
//        데이터 동기화 (save, update):
//
//        관리자가 "공공데이터 가져오기" 버튼을 누릅니다.
//
//        크롤러가 데이터를 긁어와서 save()(신규 등록) 하거나 update()(정보 갱신)를 호출해 DB를 최신화합니다.
//
//        이미지 수집 (findAllWithUrl, updateImage):
//
//        이미지 크롤러가 작동합니다.
//
//        먼저 findAllWithUrl()을 호출해 "홈페이지 주소가 있는 식당만 추려내!"라고 합니다. (주소가 없으면 사진을 못 구하니까요)
//
//        그 주소로 가서 사진을 구해오면, updateImage()를 호출해 "이 식당 ID에 이 사진 경로 저장해줘"라고 명령합니다.