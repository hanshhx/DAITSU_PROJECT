package com.example.TEAM202507_01.menus.job.repository;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 저장소(repository)' 폴더에 있다는 주소입니다.
// Repository(Mapper) 패키지는 DB와 통신하는 파일들을 모아두는 곳입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.entity.Company; // DB에 넣거나 꺼낼 때 쓸 '기업 데이터 객체'
import org.apache.ibatis.annotations.Mapper; // MyBatis 프레임워크가 제공하는 '매퍼 표시' 도구

// [3] 어노테이션(@Mapper): MyBatis의 마법 지팡이
// 의미: "스프링아, 이건 그냥 인터페이스가 아니라 SQL 쿼리랑 연결될 '매퍼'야. 구현체는 네가 알아서(XML 보고) 만들어."
// 효과: 개발자가 "class CompanyMapperImpl..." 하면서 구현 코드를 짤 필요가 없습니다.
// MyBatis가 실행 시점에 자동으로 코드를 만들어서 DB랑 연결해줍니다.
@Mapper
public interface CompanyMapper {
    // [4] 인터페이스 선언: "나는 CompanyMapper라는 이름의 DB 관리자야."
    // interface로 만든 이유는, 구체적인 SQL 실행 방법은 XML 파일에 맡기고 여기선 '기능 목록'만 정의하기 때문입니다.

    // [5] 회사 조회 메서드 (중복 확인용)
    // 기능: 회사 이름(String name)을 주면, 그 회사가 DB에 있는지 찾아봅니다.
    // 동작: 연결된 XML 파일에서 <select id="findByName"> 태그에 있는 SQL을 실행합니다.
    //       예상 SQL: SELECT * FROM COMPANY WHERE NAME = #{name}
    // 반환값:
    //   - 회사가 있으면: 그 회사 정보가 담긴 Company 객체 리턴
    //   - 회사가 없으면: null 리턴 (이걸로 중복 여부를 판단함)
    Company findByName(String name);

    // [6] 회사 정보 저장 메서드
    // 기능: 회사 정보가 담긴 객체(Company company)를 주면, DB에 새롭게 저장합니다.
    // 동작: 연결된 XML 파일에서 <insert id="save"> 태그에 있는 SQL을 실행합니다.
    //       예상 SQL: INSERT INTO COMPANY (NAME, ADDRESS, ...) VALUES (...)
    // 반환값: void (저장하고 끝이니까 돌려줄 값이 없음)
    void save(Company company);
}

//        상황: 크롤링 로봇이 '카카오' 채용 공고를 긁어왔을 때
//
//        중복 검사 (findByName):
//
//        로봇이 공고를 저장하기 전에 먼저 생각합니다. "잠깐, '카카오'라는 회사가 우리 DB에 이미 있나?"
//
//        이때 findByName("카카오")를 호출합니다.
//
//        매퍼의 역할: DB에 가서 SELECT * FROM COMPANY WHERE NAME = '카카오' 쿼리를 날려봅니다.
//
//        판단 및 저장 (save):
//
//        결과가 없으면(null): "아, 처음 보는 회사네. 등록하자!" 하고 save(new Company("카카오"...))를 호출합니다.
//
//        매퍼의 역할: DB에 INSERT INTO COMPANY ... 쿼리를 날려 회사를 저장합니다.
//
//        결과가 있으면: "이미 등록된 회사네. 저장 안 해도 되겠다." 하고 넘어갑니다.
