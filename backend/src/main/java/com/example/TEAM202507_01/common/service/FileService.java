package com.example.TEAM202507_01.common.service; // 패키지 선언

// 필요한 클래스들(Dto, Mapper, 스프링 기능 등)을 가져옵니다.
import com.example.TEAM202507_01.common.dto.FileDto;
import com.example.TEAM202507_01.common.repository.FileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j // 로그 기록용 (log.error 등 사용 가능)
@Service // "나는 비즈니스 로직을 담당하는 서비스야"라고 명찰 붙임
@RequiredArgsConstructor // final 필드(fileMapper)를 채워주는 생성자 자동 생성
public class FileService {

    private final FileMapper fileMapper; // DB 저장을 위해 매퍼를 가져옴

    // application.properties 파일에 설정된 'file.upload-dir' 값을 가져옵니다.
    // 예: C:/myproject/uploads/ (실제 파일이 저장될 경로)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 파일 저장 로직 (핵심 메서드)
     * @param files 프론트엔드에서 보낸 파일 리스트 (여러 개일 수 있음)
     * @param targetId 이 파일이 연결될 게시글 번호
     * @param category 게시판 종류 (자유게시판 등)
     */
    public void saveFiles(List<MultipartFile> files, Long targetId, String category) {
        // 1. [유효성 검사] 파일 리스트가 아예 없거나 비어있으면?
        if (files == null || files.isEmpty()) {
            return; // 할 일 없으니 그냥 돌아갑니다.
        }

        // 2. [폴더 준비] 파일을 저장할 실제 폴더 경로 객체 생성
        File dir = new File(uploadDir);
        // 만약 해당 폴더가 컴퓨터에 없다면?
        if (!dir.exists()) {
            dir.mkdirs(); // 폴더를 새로 만듭니다. (mkdirs는 상위 폴더까지 다 만들어줌)
        }

        // 3. [반복 처리] 파일 리스트를 하나씩 꺼내서 저장 작업을 시작합니다.
        for (MultipartFile file : files) {
            // 빈 껍데기 파일이면 무시하고 다음 파일로 넘어감
            if (file.isEmpty()) continue;

            try {
                // A. [원본 이름 확보] 사용자가 올린 파일명 (예: "cat.jpg")
                String originalName = file.getOriginalFilename();

                // B. [저장용 이름 생성] 중복 방지를 위해 랜덤 문자열(UUID)을 붙임
                // UUID.randomUUID().toString() -> "550e8400-e29b..." 같은 긴 문자열 생성
                // 결과: "550e8400..._cat.jpg"
                String uuid = UUID.randomUUID().toString();
                String savedName = uuid + "_" + originalName;

                // C. [전체 경로 완성] 폴더 경로 + 저장용 이름
                // 예: "C:/uploads/550e8400..._cat.jpg"
                String savePath = uploadDir + savedName;

                // D. [디스크 저장] ★ 제일 중요한 줄!
                // 메모리에 있던 파일 데이터를 실제 하드디스크 경로(savePath)로 복사합니다.
                file.transferTo(new File(savePath));

                // E. [DB 기록 준비] 파일이 잘 저장됐으니, 그 정보를 DB에 넣기 위해 DTO를 만듭니다.
                FileDto fileDto = new FileDto();
                fileDto.setTargetId(targetId);   // 게시글 번호
                fileDto.setCategory(category);   // 카테고리
                fileDto.setOriginalName(originalName); // 원래 이름
                fileDto.setSavedName(savedName); // 저장된 이름
                fileDto.setFilePath(savePath);   // 전체 경로
                fileDto.setFileSize(file.getSize()); // 파일 크기

                // F. [DB 저장] Mapper에게 시켜서 DB 테이블에 INSERT 합니다.
                fileMapper.insertFile(fileDto);

            } catch (IOException e) {
                // 저장하다가 에러(용량 부족, 권한 없음 등)가 나면 로그를 찍습니다.
                log.error("파일 저장 중 오류 발생: {}", e.getMessage());
                // 여기서 필요하다면 트랜잭션을 취소(rollback)하거나 사용자에게 에러 알림을 보낼 수도 있습니다.
            }
        }
    }
}

