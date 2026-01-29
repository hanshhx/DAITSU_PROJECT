package com.example.TEAM202507_01.menus.tour.service;

import com.example.TEAM202507_01.menus.tour.dto.TourDto;
import com.example.TEAM202507_01.menus.tour.repository.TourMapper; // Mapper Import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: 스프링에게 "이건 비즈니스 로직을 처리하는 서비스야"라고 알려줌 (Bean 등록).
// @RequiredArgsConstructor: final 변수(tourMapper)에 대한 생성자를 자동으로 만들어줌.
// @Transactional: 메서드 실행 중 에러가 나면 DB 작업을 모두 취소(롤백)해서 데이터를 보호함.
@Service
@RequiredArgsConstructor
@Transactional
public class TourServiceImpl implements TourService {

    private final TourMapper tourMapper; // 매퍼 주입 (의존성 주입)

    // 전체 조회
    @Override
    @Transactional(readOnly = true) // 읽기 전용으로 설정하면 조회 속도가 빨라짐.
    public List<TourDto> findAll() {
        return tourMapper.findAll();
    }

    // 상세 조회
    @Override
    @Transactional(readOnly = true)
    public TourDto findById(Long id) {
        TourDto tour = tourMapper.findById(id);
        // 만약 DB에 없는 ID라면? 예외를 던져서 멈춤.
        if (tour == null) {
            throw new RuntimeException("해당 관광지를 찾을 수 없습니다. ID: " + id);
        }
        return tour;
    }

    // 저장 및 수정 통합 메서드
    @Override
    public TourDto save(TourDto tour) {
        // ID가 없으면 '새 거' -> 저장(save)
        if (tour.getId() == null) {
            tourMapper.save(tour);
        } else {
            // ID가 있으면 '헌 거' -> 수정(update)
            tourMapper.update(tour);
        }
        return tour; // 처리된 객체 반환
    }

    // 삭제
    @Override
    public void delete(Long id) {
        tourMapper.delete(id);
    }
}