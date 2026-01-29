package com.example.TEAM202507_01.admin.repository;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper // MyBatis 매퍼 인터페이스임을 명시 (XML 파일과 연결됨)
public interface AdminMapper {

    // 1. 일별 방문자 수 조회
    // 파라미터로 시작일(startDate), 종료일(endDate)을 받아서, 그 기간 동안의 날짜별 방문자 수를 가져옵니다.
    // 반환값은 [{"date": "2024-01-01", "count": 10}, ...] 형태의 리스트입니다.
    List<Map<String, Object>> selectDailyVisitCount(Map<String, String> params);

    // 2. 유입 경로별 비율 조회
    // 파라미터 없이 전체 기록을 뒤져서 "네이버 100명, 구글 50명..." 이런 통계를 가져옵니다.
    List<Map<String, Object>> selectTrafficSourceStats();

    // 3. 방문 기록 저장
    // 컨트롤러에서 만든 방문자 정보 맵(params)을 받아서 DB 테이블에 한 줄 추가합니다.
    void insertVisitLog(Map<String, Object> params);

}