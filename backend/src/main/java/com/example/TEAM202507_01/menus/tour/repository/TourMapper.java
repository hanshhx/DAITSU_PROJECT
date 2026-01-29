package com.example.TEAM202507_01.menus.tour.repository;

import com.example.TEAM202507_01.menus.tour.dto.TourDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// @Mapper: MyBatis가 이 인터페이스를 보고 실제 DB 쿼리를 실행하는 객체를 자동으로 만들어준다.
@Mapper
public interface TourMapper {

    // 1. 전체 목록 조회: DB의 모든 관광지를 가져옴.
    List<TourDto> findAll();

    // 2. 상세 조회: ID로 특정 관광지 하나를 가져옴.
    TourDto findById(Long id);

    // 3. 저장: 새로운 관광지를 DB에 넣음 (Insert).
    void save(TourDto tour);

    // 4. 수정: 기존 관광지 정보를 바꿈 (Update).
    void update(TourDto tour);

    // 5. 삭제: 관광지를 지움 (Delete).
    void delete(Long id);
}
