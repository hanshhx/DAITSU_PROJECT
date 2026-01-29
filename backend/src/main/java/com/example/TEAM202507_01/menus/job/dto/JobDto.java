package com.example.TEAM202507_01.menus.job.dto;

import com.example.TEAM202507_01.menus.job.entity.JobPost;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDto {
    private Long id; // 게시글 번호
    private String category; // 게시판 카테고리 (JOBS)
    private String title; // 공고 제목
    private String companyName; // 회사 이름
    private String companyType; // 회사 형태 (중소, 대기업 등)
    private String description;// 본문 내용 (크롤링 시엔 지역 정보로 씀)


    private String careerLevel; // 경력 사항 (DB 컬럼명과 매칭)

    private String education; // 학력 사항
    private String deadline; // 마감일 (날짜 계산 안 하고 문자로 저장)

    // 🚨 핵심 유지: DB의 link 데이터를 프론트엔드에선 'url'로 인식하게 함
    @JsonProperty("url")
    private String link; // 사람인 공고 원본 링크 (JSON에선 url로 나감)

    private int isActive; // 공고 진행 여부 (1: 진행중, 0: 마감)

    // 이 메서드는 careerLevel이라는 변수값을 꺼낼 때 getCareer()라는 이름으로 꺼내게 해줌.
    // 프론트엔드에서 career라는 이름으로 데이터를 찾고 있어서 만든 호환용 메서드임.
    public String getCareer() {
        return careerLevel;
    }
}

//
//상황 1: 취준생이 채용 정보를 검색할 때 (JobDto)
//
//사용자가 "삼성전자"를 검색합니다.
//
//서버는 DB에 저장해둔 크롤링 데이터를 꺼냅니다. (
//JobPost 엔티티)
//
//이걸 그대로 보내지 않고 JobDto 상자에 옮겨 담습니다.
//
//이때, 자바에서는 link라고 부르지만 프론트엔드는 url이라는 이름으로 달라고 떼를 씁니다.
//
//그래서 @JsonProperty("url")을 붙여서 이름표를 바꿔줍니다.
//
//        또, 프론트엔드는 경력을 career라고 찾는데 우리는 careerLevel이라고 저장했습니다.
//
//        그래서 getCareer() 메서드를 만들어 "여기 있어~" 하고 챙겨줍니다.
//
//사용자 화면에 공고가 뜹니다.
//
//상황 2: 취준생이 자기소개서를 올릴 때 (JobUserPostDto)
//
//사용자가 "열정 있는 신입 개발자입니다!"라고 글을 씁니다.
//
//이 내용이 JobUserPostDto 상자에 담겨서 서버로 날아옵니다.
//
//        서버는 "작성자가 누구지?(userId), 내용은 뭐지?(description)" 확인하고 DB에 저장합니다.