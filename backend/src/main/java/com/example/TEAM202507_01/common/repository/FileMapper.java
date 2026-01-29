package com.example.TEAM202507_01.common.repository; // 패키지 선언

import com.example.TEAM202507_01.common.dto.FileDto; // 위에서 만든 FileDto 가져옴
import org.apache.ibatis.annotations.Mapper; // MyBatis 매퍼 어노테이션

@Mapper // "이 인터페이스는 MyBatis XML 파일과 연결된 DB 작업 명세서야"라고 스프링에게 알림
public interface FileMapper {
    // 파일 정보 저장 메서드
    // FileDto에 담긴 정보를 DB 테이블에 INSERT 하는 쿼리를 호출합니다.
    // 실제 SQL은 XML 파일에 <insert id="insertFile"> 태그로 작성되어 있습니다.
    void insertFile(FileDto fileDto);
}