package com.example.TEAM202507_01.menus.job.repository;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 저장소(repository)' 폴더에 위치한다는 주소입니다.
// Repository 패키지는 DB와 직접 대화하는 파일(Mapper, Dao)들을 모아두는 곳입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.entity.JobPost; // DB에 넣거나 뺄 때 쓸 '채용 공고 데이터 상자'
import org.apache.ibatis.annotations.Mapper; // MyBatis가 "이건 매퍼야"라고 인식하게 해주는 도구
import org.apache.ibatis.annotations.Param; // SQL에 변수 이름을 전달할 때 쓰는 이름표
import java.util.List; // 여러 개의 데이터를 담을 리스트 바구니

// [3] 어노테이션(@Mapper): MyBatis의 핵심 표시
// 의미: "스프링아, 이 인터페이스는 그냥 코드가 아니라, XML에 있는 SQL문이랑 연결될 'DB 관리자'야."
// 효과: 개발자가 직접 SQL 실행 코드를 짜지 않아도, MyBatis가 알아서 구현체를 만들어줍니다.
@Mapper
public interface JobMapper {
    // [4] 인터페이스 선언: "나는 JobMapper라는 이름의 DB 관리자입니다."
    // interface로 만든 이유: "무슨 기능이 있다"만 정의하면, 실제 동작은 MyBatis가 XML을 보고 알아서 처리하기 때문입니다.

    // [5] 공고 목록 조회 (검색 기능 포함)
    // 기능: 사용자가 입력한 검색어(keyword), 경력(career), 학력(education) 조건에 맞는 공고들을 찾아옵니다.
    // 어노테이션(@Param):
    // - 자바 변수명은 "keyword"지만, XML 파일에서 #{keyword}라고 썼을 때 서로 짝을 맞춰주기 위해 이름표를 붙입니다.
    // - 파라미터가 2개 이상일 때는 @Param을 붙여주는 게 안전하고 확실합니다.
    // 반환값: List<JobPost> (찾아낸 공고들을 리스트에 담아서 줍니다. 없으면 빈 리스트.)
    List<JobPost> findAll(@Param("keyword") String keyword,
                          @Param("career") String career,
                          @Param("education") String education);

    // [6] 상세 조회
    // 기능: 공고의 고유 번호(id)를 주면, 그 공고 하나만 딱 집어서 가져옵니다.
    // 용도: 목록에서 클릭해서 '상세 페이지'로 들어갈 때 사용합니다.
    // 반환값: JobPost (찾은 공고 데이터 객체 하나. 없으면 null.)
    JobPost findById(Long id);

    // [7] 공고 저장 (크롤링 데이터용)
    // 기능: 크롤링해온 공고 데이터(JobPost 객체)를 DB에 새롭게 저장(INSERT)합니다.
    // 파라미터: JobPost jobPost (제목, 회사명, 링크 등이 꽉 채워진 데이터 상자)
    // 반환값: void (저장만 하고 끝내니까 돌려줄 값이 없습니다.)
    void insertJobPost(JobPost jobPost);

    // [8] 공고 수정
    // 기능: 이미 저장된 공고의 내용을 고칩니다(UPDATE).
    // 용도: 마감일이 연장되었거나, 오타가 있어서 수정할 때 사용합니다.
    void updateJobPost(JobPost jobPost);

    // [9] [중복 방지용] 카운트 조회
    // 기능: "이 회사(companyName)가 올린 이 제목(title)의 공고가 DB에 몇 개나 있어?"라고 물어봅니다.
    // 용도: 크롤링 로봇이 똑같은 공고를 또 긁어와서 저장하는 걸 막기 위해, 저장 전에 먼저 검사할 때 씁니다.
    // 반환값: int (개수).
    // - 0이면: "아직 없는 공고네? 저장하자!"
    // - 1 이상이면: "이미 있는 공고네. 패스!"
    int countByCompanyAndTitle(@Param("companyName") String companyName, @Param("title") String title);

    // [10] 전체 개수 세기
    // 기능: DB에 저장된 모든 공고가 총 몇 개인지 셉니다.
    // 용도: "총 1,234건의 채용 공고가 있습니다"라고 화면에 보여줄 때 씁니다.
    int countAll();

    // [11] 전체 조회 (검색 조건 없이)
    // 기능: 조건 없이 모든 공고를 다 가져옵니다.
    // 용도: 관리자 페이지에서 전체 목록을 보거나, 엑셀 다운로드 등을 할 때 쓸 수 있습니다.
    // (현재 코드에서는 파라미터가 없는 걸로 보아, 필터 없는 순수 전체 목록 조회용입니다.)
    List<JobPost> findAllSearch();
}

//
//        상황 1: 사용자가 "대전" 지역의 "신입" 공고를 검색할 때
//
//        검색 요청: 사용자가 검색창에 "대전", 경력 필터에 "신입"을 넣고 엔터를 칩니다.
//
//        조회 (findAll):
//
//        서비스가 jobMapper.findAll("대전", "신입", null)을 호출합니다.
//
//        매퍼는 XML 파일로 가서 SELECT * FROM JOB_POST WHERE TITLE LIKE '%대전%' AND CAREER LIKE '%신입%' 같은 쿼리를 실행합니다.
//
//        DB에서 찾은 결과들을 List<JobPost>에 담아서 서비스에게 건네줍니다.
//
//        상황 2: 크롤링 로봇이 "카카오" 공고를 긁어왔을 때
//
//        중복 검사 (countByCompanyAndTitle):
//
//        로봇이 무작정 저장하지 않고, "잠깐, '카카오'의 '백엔드 개발자' 공고가 이미 있나?" 하고 물어봅니다.
//
//        매퍼는 SELECT COUNT(*) ...를 실행해서 숫자를 세어줍니다.
//
//        저장 (insertJobPost):
//
//        숫자가 0이면(없으면), "새로운 공고다!" 하고 insertJobPost를 호출해 DB에 저장합니다.