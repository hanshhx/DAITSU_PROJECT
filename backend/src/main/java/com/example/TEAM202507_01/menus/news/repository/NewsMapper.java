package com.example.TEAM202507_01.menus.news.repository;

import com.example.TEAM202507_01.menus.news.dto.NewsDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

//// @Mapper: MyBatis에게 "이 인터페이스는 DB 쿼리와 연결되는 매퍼야"라고 알려줌.
/// 스프링 실행 시 이 인터페이스의 구현체를 자동으로 만들어 Bean으로 등록함.
@Mapper
public interface NewsMapper {

    // 1. 전체 조회
    // // DB에서 모든 뉴스를 가져와서 List<NewsDto> 형태로 반환함.
    List<NewsDto> findAll();

    // 2. 상세 조회
    // // 특정 ID(Long id)를 가진 뉴스 하나를 찾아서 NewsDto로 반환함.
    NewsDto findById(Long id);

    // 3. 등록 (Insert)
    // 새로운 뉴스(NewsDto)를 DB에 저장함. 반환값이 void이므로 결과만 실행함.
    void save(NewsDto news);

    // 4. 수정 (Update)
    // 기존 뉴스 내용을 수정함.
    void update(NewsDto news);

    // 5. 삭제 (Delete)
    //// 특정 ID의 뉴스를 DB에서 삭제함.
    void delete(Long id);
}