package com.example.TEAM202507_01.common.dto; // 이 파일의 주소(패키지)

import lombok.Data; // 롬복 라이브러리 (Getter, Setter 자동 생성)

@Data // @Getter, @Setter, @ToString 등을 한방에 해결해주는 어노테이션
public class FileDto {
    private Long fileId;       // PK: 파일의 고유 번호 (DB에서 자동 생성됨)
    private Long targetId;     // 게시글 ID: 이 파일이 어느 게시글(혹은 댓글)에 붙은 건지 연결 고리
    private String category;   // 카테고리: "free_board", "notice" 등 어디서 올린 파일인지 구분
    private String originalName; // 사용자가 올린 원래 파일 이름 (예: "내사진.jpg") - 다운로드할 때 보여줄 이름
    private String savedName;    // 서버에 저장된 진짜 이름 (예: "uuid_내사진.jpg") - 중복 방지용
    private String filePath;     // 파일이 저장된 전체 경로 (예: "C:/uploads/uuid_내사진.jpg")
    private Long fileSize;       // 파일 크기 (바이트 단위)
}