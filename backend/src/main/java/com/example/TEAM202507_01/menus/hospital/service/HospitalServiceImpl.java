package com.example.TEAM202507_01.menus.hospital.service;
// [1] 패키지 선언: 이 파일이 '병원 메뉴 > 서비스' 폴더에 있다는 주소입니다.

// [2] 임포트: 필요한 도구들(DTO, Mapper, 스프링 기능 등)을 가져옵니다.
import com.example.TEAM202507_01.menus.hospital.dto.HospitalDto;
import com.example.TEAM202507_01.menus.hospital.dto.HospitalMapDto;
import com.example.TEAM202507_01.menus.hospital.repository.HospitalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
// [3] 어노테이션(@Service): 스프링에게 명찰을 보여줍니다.
// 의미: "저(HospitalServiceImpl)는 비즈니스 로직을 담당하는 정식 직원(Bean)입니다. 필요할 때 불러주세요."
// 효과: 이게 없으면 컨트롤러에서 Autowired나 생성자 주입으로 이 서비스를 가져올 수 없습니다. (에러 발생)

@RequiredArgsConstructor
// [4] 어노테이션(@RequiredArgsConstructor): 롬복 기능입니다.
// 의미: "final이 붙은 변수(hospitalMapper)를 채워주는 생성자 코드를 네가 알아서 짜줘."
// 효과: 코드가 깔끔해지고, 의존성 주입(DI)을 안전하게 할 수 있습니다.

@Transactional
// [5] 어노테이션(@Transactional): 데이터 안전 장치입니다.
// 의미: "이 클래스 안의 모든 메서드는 '거래(Transaction)'로 취급한다."
// 효과: 만약 데이터를 저장하다가 중간에 에러가 나면, 저장하기 전 상태로 모든 것을 되돌립니다(Rollback). 데이터가 꼬이는 걸 막아줍니다.

public class HospitalServiceImpl implements HospitalService {
    // [6] 클래스 선언: HospitalService 인터페이스를 구현(implements)합니다.
    // 의미: "저는 HospitalService 메뉴판에 적힌 모든 기능을 빠짐없이 책임지고 수행하겠습니다."

    private final HospitalMapper hospitalMapper;
    // [7] 의존성 주입: DB와 대화할 창고지기(Mapper)를 모셔옵니다.
    // final: "이 창고지기는 절대 바뀌지 않습니다." (필수 인력)

    // ==========================================
    // 1. 전체 목록 조회 (Stream 문법 사용)
    // ==========================================
    @Override // "인터페이스에 있는 거 제가 구현한 겁니다" 표시
    @Transactional(readOnly = true)
    // [8] 읽기 전용 모드 설정
    // 의미: "이 메서드는 조회만 하고 데이터 변경은 안 해요."
    // 효과: 스프링이 "아, 변경 감지 안 해도 되네?" 하고 성능을 최적화해줍니다. 빨라집니다.
    public List<HospitalDto> findAll() {

        // [9] DB 조회 및 데이터 가공 파이프라인 시작
        return hospitalMapper.findAll() // (1) 매퍼에게 시켜서 DB의 모든 병원 데이터를 리스트로 가져옵니다.

                .stream() // (2) 가져온 리스트를 '스트림(Stream)'이라는 컨베이어 벨트에 올립니다.
                // for문보다 세련되고, 데이터 변환 작업을 연달아하기 좋습니다.

                .map(this::convertToDto)
                // (3) 벨트 위를 지나가는 병원 데이터 하나하나를 잡아서 'convertToDto' 메서드로 보냅니다.
                // 즉, [원본 데이터] -> [convertToDto] -> [가공된 데이터] 로 변신시킵니다.
                // this::convertToDto는 "이 클래스 안에 있는 convertToDto 메서드를 써라"라는 뜻입니다.

                .collect(Collectors.toList());
        // (4) 가공이 끝난 데이터들을 다시 주워 담아서 새로운 리스트(List)로 만듭니다.
        // 그리고 그 리스트를 컨트롤러에게 반환합니다.
    }

    @Override
    public List<HospitalMapDto> findInfo() {
        // [10] 지도용 데이터 조회
        // 이건 별도의 가공 없이 매퍼가 준 그대로 토스합니다. (지도용 DTO가 이미 가벼우니까)
        return hospitalMapper.findInfo();
    }

    // ==========================================
    // 2. 상세 조회
    // ==========================================
    @Override
    @Transactional(readOnly = true) // 역시 조회니까 읽기 전용으로 최적화
    public HospitalDto findById(Long id) {

        // [11] 매퍼에게 "ID가 이거인 병원 찾아와"라고 시킵니다.
        HospitalDto hospital = hospitalMapper.findById(id);

        // [12] 유효성 검사 (Null Check)
        // 만약 DB에 없는 ID(예: 9999번)를 요청했다면 hospital은 null일 겁니다.
        if (hospital == null) {
            // 그냥 null을 주면 프론트엔드가 곤란해하니까, 명확하게 에러를 터뜨립니다.
            throw new RuntimeException("병원을 찾을 수 없습니다.");
        }

        // [13] 찾아온 데이터를 가공(평점 추가 등)해서 반환합니다.
        return convertToDto(hospital);
    }

    // ==========================================
    // 3. 저장 (등록/수정)
    // ==========================================
    @Override
    public HospitalDto save(HospitalDto hospital) {
        // [14] 신규 등록인지 수정인지 판단하는 로직
        // ID가 없으면(null) -> 새로 만드는 거구나 -> INSERT
        // ID가 있으면(1, 2...) -> 원래 있던 거 고치는구나 -> UPDATE

        if (hospital.getId() == null) {
            // [15] 신규 등록: 매퍼의 save(Insert 쿼리)를 실행합니다.
            hospitalMapper.save(hospital);
        } else {
            // [16] 수정: 원래는 update를 실행해야 하지만, 현재 코드에선 주석 처리되어 있네요.
            // 필요하면 주석을 풀어서 쓰면 됩니다.
            // hospitalMapper.update(hospital);
        }

        // [17] 저장된 정보를 다시 돌려줍니다. (보통 저장 후 ID가 생긴 객체를 확인용으로 리턴함)
        return hospital;
    }

    // [추가] 인터페이스의 delete 메서드 구현
    @Override
    public void delete(Long id) {
        // [18] 매퍼에게 "이 ID 가진 데이터 삭제해"라고 Delete 쿼리를 날립니다.
        hospitalMapper.delete(id);
    }

    // ==========================================
    // 4. 변환 메서드 (Entity -> DTO)
    // ==========================================
    // 이 메서드는 외부에서 부르는 게 아니라 내부에서만 쓰는 도우미(Helper)입니다.
    private HospitalDto convertToDto(HospitalDto hospital) {

        // [19] 기본값 설정
        // 현재 DB 테이블에는 평점/리뷰 수가 없어서 일단 0으로 초기화합니다.
        // 나중에 리뷰 기능을 붙이면, 여기서 리뷰 테이블을 조회해서 평균을 계산하는 코드를 넣으면 됩니다.
        Double averageRating = 0.0; // 평점 0.0점
        Integer reviewCount = 0;    // 리뷰 0개

        // [20] 빌더 패턴을 사용한 데이터 복사 & 조립
        // 원본(hospital)에 있는 데이터를 꺼내서, 새로운 DTO 객체에 옮겨 담습니다.
        // 동시에 위에서 만든 평점 정보도 같이 끼워 넣습니다.
        return HospitalDto.builder()
                .id(hospital.getId())              // ID 복사
                .category(hospital.getCategory())  // 카테고리 복사
                .name(hospital.getName())          // 이름 복사
                .treatCategory(hospital.getTreatCategory()) // 진료과목 복사
                .address(hospital.getAddress())    // 주소 복사
                .tel(hospital.getTel())            // 전화번호 복사
                .editDate(hospital.getEditDate())  // 수정일 복사
                .averageRating(averageRating)      // ★ 계산된 평점 추가
                .reviewCount(reviewCount)          // ★ 계산된 리뷰 수 추가
                .build(); // "완성된 객체 주세요!"
    }
}

//
//주문 접수 (findAll):
//
//컨트롤러가 "손님이 병원 목록 달라는데요?" 하고 findAll()을 호출합니다.
//
//재료 요청 (Mapper Call):
//
//이 서비스는 매퍼(hospitalMapper)에게 "창고(DB)에 있는 병원 데이터 다 꺼내줘"라고 시킵니다.
//
//매퍼가 HospitalDto 원본 리스트를 가져옵니다.
//
//손질 (Stream &Map):
//
//가져온 데이터는 아직 날것입니다. (예: 평점이 계산 안 되어 있거나 함)
//
//stream()이라는 컨베이어 벨트에 데이터를 하나씩 올립니다.
//
//convertToDto라는 가공 기계를 통과시키면서 평점(0.0)과 리뷰 수(0)를 세팅해서 예쁘게 포장합니다.
//
//서빙 (Return):
//
//포장이 끝난 데이터들을 다시 박스(List)에 담아서 컨트롤러에게 전달합니다.