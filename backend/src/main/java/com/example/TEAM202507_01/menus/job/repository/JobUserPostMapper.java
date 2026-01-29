package com.example.TEAM202507_01.menus.job.repository;
// [1] 패키지 선언: 이 파일이 '일자리(job) 메뉴 > 저장소(repository)' 폴더에 있다는 주소입니다.
// Repository 패키지는 DB와 직접 대화하는 파일(Mapper)들이 모여있는 곳입니다.

// [2] 임포트: 필요한 도구들을 가져옵니다.
import com.example.TEAM202507_01.menus.job.entity.JobUserPost; // DB 테이블과 똑같이 생긴 '구직 공고 원본 데이터'
import org.apache.ibatis.annotations.Mapper; // MyBatis가 "이건 매퍼야"라고 인식하게 해주는 도구
import java.util.List; // 데이터를 여러 개 담아서 나를 때 쓰는 리스트 바구니

// [3] 어노테이션(@Mapper): MyBatis의 핵심 표시
// 의미: "스프링아, 이 인터페이스는 껍데기일 뿐이지만, 나중에 XML 파일이랑 연결해서 실제 DB 관리자(구현체)로 만들어줘."
// 효과: 개발자가 SQL 실행 코드를 일일이 짜지 않아도, 메서드 이름만 맞추면 알아서 DB랑 통신이 됩니다.
@Mapper
public interface JobUserPostMapper {
    // [4] 인터페이스 선언: "나는 JobUserPostMapper라는 이름의 DB 관리자입니다."
    // 역할: 사용자 구직 공고(JobUserPost)에 관련된 모든 DB 작업을 총괄합니다.

    // [5] 전체 목록 조회
    // 기능: DB에 저장된 모든 사용자의 구직 글을 다 가져옵니다.
    // 반환값: List<JobUserPost>
    // - 여러 개의 글을 가져오니까 List에 담습니다.
    // - DTO가 아니라 Entity(JobUserPost)를 반환하는 이유는, 매퍼는 DB랑 1:1로 대화하는 애라서 원본 데이터를 다루는 게 맞기 때문입니다.
    List<JobUserPost> findAll();

    // [6] 상세 조회 (글 하나만 보기)
    // 기능: 글 번호(id)를 주면, 딱 그 글 하나만 찾아서 가져옵니다.
    // 용도: 목록에서 제목을 클릭했을 때 '상세 내용'을 보여주기 위해 씁니다.
    // 반환값: JobUserPost (글 하나니까 List 아님. 없으면 null 반환)
    JobUserPost findById(Long id);

    // [7] 구직 공고 등록 (저장)
    // 기능: 사용자가 쓴 새 글(JobUserPost 객체)을 DB에 저장(INSERT)합니다.
    // 파라미터: JobUserPost jobUserPost (제목, 내용, 작성자ID 등이 꽉 찬 데이터 상자)
    // 반환값: void (저장하고 끝. 돌려줄 거 없음)
    void insertJobUserPost(JobUserPost jobUserPost);

    // [8] 구직 공고 수정
    // 기능: 이미 저장된 글의 내용을 변경(UPDATE)합니다.
    // 용도: 사용자가 "아, 오타 났다" 하고 내용을 고치거나, "저 취업했어요" 하고 상태(isActive)를 바꿀 때 씁니다.
    // 파라미터: JobUserPost jobUserPost (수정할 내용이 담긴 데이터 상자. 반드시 ID가 들어있어야 함)
    void updateJobUserPost(JobUserPost jobUserPost);
}
//
//        상황 1: 취준생이 "저 일자리 구해요!"라고 글을 올릴 때
//
//        작성: 사용자가 제목("열정맨입니다"), 내용("자바 잘해요")을 입력하고 [등록]을 누릅니다.
//
//        전달: 컨트롤러 -> 서비스를 거쳐, 데이터가 JobUserPost라는 엔티티 상자에 담겨서 이 매퍼에게 도착합니다.
//
//        저장 (insertJobUserPost):
//
//        매퍼는 "아, 새 글이구나!" 하고 INSERT INTO JOB_USER_POST... 쿼리를 실행해 DB에 저장합니다.
//
//        상황 2: 기업 인사담당자가 인재 목록을 볼 때
//
//        요청: 인사담당자가 "인재 찾기" 메뉴를 클릭합니다.
//
//        조회 (findAll):
//
//        서비스가 매퍼에게 findAll()을 호출합니다.
//
//        매퍼는 DB에 있는 모든 구직 글을 긁어와서 List<JobUserPost>로 묶어 서비스에게 건네줍니다.