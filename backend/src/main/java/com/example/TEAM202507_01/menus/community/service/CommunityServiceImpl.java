package com.example.TEAM202507_01.menus.community.service;

import com.example.TEAM202507_01.cleanbot.service.CleanBotService;
import com.example.TEAM202507_01.menus.community.dto.CommentDto;
import com.example.TEAM202507_01.menus.community.dto.CommunityDto;
import com.example.TEAM202507_01.menus.community.repository.CommentMapper;
import com.example.TEAM202507_01.menus.community.repository.CommunityMapper;
import com.example.TEAM202507_01.user.repository.MyPageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;
    private final CommentMapper commentMapper;
    private final MyPageMapper myPageMapper;
    private final CleanBotService cleanBotService; // 클린봇 서비스 주입

    // 파일 저장 경로 (프로젝트 폴더 내 uploads 폴더)
    private final Path UPLOAD_PATH = Paths.get("uploads").toAbsolutePath();

    // ====================================================
    // 1. 게시글 저장 (파일 저장 로직 활성화!)
    // ====================================================
    @Override
    @Transactional
    public long savePost(CommunityDto dto, List<MultipartFile> files) {
        // 1. [CleanBot] 내용 검사
        if (cleanBotService != null) {
            log.info("🤖 [CleanBot] 게시글 텍스트 검증 시작");
            cleanBotService.checkContent(dto.getTitle()); // 제목 검사
            cleanBotService.checkContent(dto.getContent()); // 에디터 본문 검사
        }

        // 2. 유저 ID 변환 (로그인 ID -> UUID)
        String uuid = myPageMapper.findUuidByLoginId(dto.getUserId());
        if (uuid != null) dto.setUserId(uuid);

        System.out.println("게시글 등록 요청: " + dto);

        // 3. 게시글 DB 저장
        communityMapper.insertPost(dto);
        Long postId = dto.getId(); // 저장된 글 번호(PK) 가져오기

        // 4. 🔥 [수정됨] 파일 저장 로직 (주석 해제 완료!)
        if (files != null && !files.isEmpty()) {
            File dir = UPLOAD_PATH.toFile();
            // 폴더가 없으면 생성
            if (!dir.exists()) dir.mkdirs();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    String originalName = file.getOriginalFilename();

                    // 확장자 추출 (안전하게 처리)
                    String ext = "";
                    if (originalName != null && originalName.contains(".")) {
                        ext = originalName.substring(originalName.lastIndexOf("."));
                    }

                    // 서버에 저장될 파일명 (UUID + 확장자) -> 중복 방지
                    String savedName = UUID.randomUUID().toString() + ext;

                    // 실제 파일 저장 (uploads 폴더에)
                    file.transferTo(new File(dir, savedName));

                    // DB에 파일 정보 저장 (웹 접근 경로는 /images/...)
                    // insertFile 파라미터 순서: targetId, category, originalName, savedName, filePath
                    communityMapper.insertFile(
                            postId,
                            dto.getCategory(),
                            originalName,
                            savedName,
                            "/images/" + savedName
                    );

                    log.info("📁 파일 저장 완료: {}", originalName);

                } catch (IOException e) {
                    log.error("파일 저장 중 오류 발생", e);
                    // 파일 하나 실패해도 게시글 저장은 유지 (필요 시 throw로 변경 가능)
                }
            }
        }
        return postId;
    }

    // ====================================================
    // 2. 댓글 저장
    // ====================================================
    @Override
    @Transactional
    public void saveComment(CommentDto dto) {
        // [CleanBot] 댓글 필터링
        if (cleanBotService != null) {
            log.info("🤖 [CleanBot] 댓글 필터링 시작");
            cleanBotService.checkContent(dto.getContent());
        }

        // 유저 ID 변환
        String uuid = myPageMapper.findUuidByLoginId(dto.getUserId());
        if(uuid != null) dto.setUserId(uuid);

        // DB 저장
        commentMapper.save(dto);
    }

    // ====================================================
    // 3. 조회 및 기타 기능 (기존 유지)
    // ====================================================

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDto> getPostList(String category, int page, int size) {
        int offset = (page - 1) * size;
        if (category == null || "ALL".equalsIgnoreCase(category)) {
            return communityMapper.selectAllPosts(offset, size);
        }
        return communityMapper.selectPostsByCategoryPaging(category, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityDto findPostById(Long id, String userId) {
        communityMapper.viewCountIncrease(id);
        CommunityDto dto = communityMapper.selectPostById(id);

        // Null 방지 및 좋아요 여부 확인
        dto.setIsLiked(false);
        if (userId != null) {
            String uuid = myPageMapper.findUuidByLoginId(userId);
            String targetUserId = (uuid != null) ? uuid : userId;

            int count = communityMapper.likeExists(id, targetUserId);
            dto.setIsLiked(count > 0);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getFilePathsByPostId(Long postId) {
        // DB에서 해당 게시글의 파일 경로 목록 조회
        return communityMapper.selectFilePathsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDto> getOtherPostsByUser(String userId, Long currentPostId) {
        return communityMapper.selectOtherPostsByUserId(userId, currentPostId);
    }

    @Override
    public String uploadEditorImage(MultipartFile file) {
        if (file.isEmpty()) return null;
        try {
            File dir = UPLOAD_PATH.toFile();
            if (!dir.exists()) dir.mkdirs();
            String savedName = UUID.randomUUID().toString() + ".jpg";
            file.transferTo(new File(dir, savedName));
            return "/images/" + savedName;
        } catch (IOException e) {
            throw new RuntimeException("에디터 이미지 업로드 실패", e);
        }
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        communityMapper.deletePost(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findCommentsByPostId(Long postId) {
        return communityMapper.selectCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        communityMapper.deleteComment(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDto> findPostsByCategory(String category) {
        return communityMapper.selectPostsByCategoryPaging(category, 0, 100);
    }

    @Override
    public boolean isUserLiked(Long id, String userId){
        int count = communityMapper.likeExists(id, userId);
        return count >= 1;
    }

    @Override
    public void likeIncrease(Long id, String userId) {
        int count = communityMapper.likeExists(id, userId);
        if (count < 1) {
            communityMapper.likeIncrease(id, userId);
        } else {
            communityMapper.likeDecrease(id, userId);
        }
    }

    @Override
    public int likeCount(Long id) {
        return communityMapper.likeCount(id);
    }

    @Override
    public void deleteAllLike(Long id) {
        communityMapper.deleteAllLike(id);
    }

    @Override
    public void deleteAllComment(Long id){
        communityMapper.deleteAllComment(id);
    }
}