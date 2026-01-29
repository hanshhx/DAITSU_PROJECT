package com.example.TEAM202507_01.menus.hospital.repository; // 패키지명 확인

import com.example.TEAM202507_01.menus.hospital.dto.HospitalDto;
import com.example.TEAM202507_01.menus.hospital.dto.HospitalMapDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper // MyBatis가 구현체를 자동 생성함
public interface HospitalMapper {

    // 1. 병원 전체 목록 조회
    List<HospitalDto> findAll();
     // XML의 <select id="findAll">과 연결됨. 모든 병원 목록을 가져옴.

    List<HospitalMapDto> findInfo();
    //// XML의 <select id="findInfo">와 연결됨. 지도용 간략 정보를 가져옴.

    // 2. 병원 상세 조회
    HospitalDto findById(Long id);
    // XML의 <select id="findById">와 연결됨. 특정 병원 하나만 조회함.

    // 3. 병원 등록 (Insert)
    void save(HospitalDto hospital);
    //// XML의 <insert id="save">와 연결됨. 병원 정보를 저장함

    // 4. 병원 정보 수정 (Update)
    void update(HospitalDto hospital);
    // XML의 <update id="update">와 연결됨. 병원 정보를 수정함.

    // 5. 병원 삭제 (Delete)
    void delete(Long id);
    //XML의 <delete id="delete">와 연결됨. 병원을 삭제함
}