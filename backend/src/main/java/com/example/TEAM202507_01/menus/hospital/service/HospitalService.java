package com.example.TEAM202507_01.menus.hospital.service;
// [1] 패키지 선언: 이 파일이 '병원 메뉴 > 서비스(service)' 폴더에 위치한다는 주소입니다.

// [2] 임포트: 이 메뉴판에서 다룰 데이터 가방(DTO)들과 리스트 도구를 가져옵니다.
import com.example.TEAM202507_01.menus.hospital.controller.HospitalController;
import com.example.TEAM202507_01.menus.hospital.dto.HospitalDto;     // 병원 상세 정보 가방
import com.example.TEAM202507_01.menus.hospital.dto.HospitalMapDto;  // 지도용 간략 정보 가방
import net.bytebuddy.implementation.Implementation;

import java.util.List; // 데이터를 여러 개 담을 수 있는 리스트 도구

// [3] 인터페이스 선언: "나는 HospitalService라는 이름의 메뉴판이야."
// class가 아니라 'interface'입니다. 껍데기만 정의한다는 뜻입니다.
public interface HospitalService {

    // --- [메뉴 목록 정의] ---
    // 여기 적힌 메서드들은 나중에 'HospitalServiceImpl' 클래스가 반드시(!!!) 구현해야 합니다.
    // 안 그러면 에러가 나서 프로그램 실행이 안 됩니다.

    // [4] 전체 목록 조회 메뉴
    // 의미: "병원 정보를 몽땅 가져오는 기능이 있어야 해."
    // 반환값: List<HospitalDto> (병원 정보가 담긴 리스트)
    List<HospitalDto> findAll();

    // [5] 지도용 정보 조회 메뉴
    // 의미: "지도에 뿌릴 가벼운 정보만 가져오는 기능도 있어야 해."
    // 반환값: List<HospitalMapDto> (지도용 정보 리스트 - 이름, 좌표주소 등만 있음)
    List<HospitalMapDto> findInfo();

    // [6] 상세 조회 메뉴
    // 의미: "ID(번호)를 주면 그 병원 하나만 찾아오는 기능이 있어야 해."
    // 파라미터: Long id (찾을 병원의 고유 번호)
    // 반환값: HospitalDto (찾은 병원 정보 하나)
    HospitalDto findById(Long id);

    // [7] 저장(등록/수정) 메뉴
    // 의미: "병원 정보를 주면 DB에 저장하거나 수정하는 기능이 있어야 해."
    // 파라미터: HospitalDto (저장할 병원 정보가 담긴 가방)
    // 반환값: HospitalDto (저장 완료된 후의 병원 정보 - 주로 ID가 채워져서 나옴)
    HospitalDto save(HospitalDto hospital);

    // [8] 삭제 메뉴
    // 의미: "ID(번호)를 주면 그 병원을 없애버리는 기능이 있어야 해."
    // 파라미터: Long id (삭제할 병원의 고유 번호)
    // 반환값: void (삭제하고 나면 돌려줄 게 없으니 없음)
    void delete(Long id);
}

//        컨트롤러의 요청:
//
//        병원 담당 컨트롤러(HospitalController)가 일을 하려고 합니다.
//
//        컨트롤러는 복잡한 로직을 직접 짜기 싫어서 서비스(HospitalService)를 부릅니다.
//
//        메뉴 확인 (Interface):
//
//        컨트롤러는 이 파일(HospitalService)을 보고 "아, findAll 기능이랑 save 기능이 있구나" 하고 확인합니다.
//
//        실제 작업 (Implementation):
//
//        컨트롤러가 hospitalService.findAll()을 호출하면, 실제로는 이 인터페이스 뒤에 숨어있는 **구현체(HospitalServiceImpl)**가 튀어나와서 일을 합니다.
//
//        유연성:
//
//        만약 나중에 "일하는 방식"을 바꾸고 싶으면, 이 메뉴판(인터페이스)은 그대로 두고 뒤에 있는 직원(구현체)만 갈아끼우면 됩니다. 그래서 스프링에서는 이렇게 인터페이스를 먼저 만드는 것을 권장합니다.