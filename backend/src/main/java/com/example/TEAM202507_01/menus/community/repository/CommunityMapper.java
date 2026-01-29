package com.example.TEAM202507_01.menus.community.repository;

import com.example.TEAM202507_01.menus.community.dto.CommentDto;
import com.example.TEAM202507_01.menus.community.dto.CommunityDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Mapper
public interface CommunityMapper {

    // ★ [추가됨] 검색 엔진 마이그레이션용 전체 조회 (페이징 없음)
    List<CommunityDto> findAll();

    // ------------------- 조회 관련 -------------------

    // 전체 목록 조회 (페이징)
    List<CommunityDto> selectAllPosts(@Param("offset") int offset, @Param("size") int size);

    // 카테고리별 목록 조회 (페이징)
    List<CommunityDto> selectPostsByCategoryPaging(
            @Param("category") String category,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 단순 카테고리 조회 (기존 코드 호환용)
    List<CommunityDto> selectPostsByCategory(@Param("category") String category);

    // 상세 조회
    CommunityDto selectPostById(Long id);

    // 상세 페이지용 파일 경로 조회
    List<String> selectFilePathsByPostId(@Param("postId") Long postId);

    // 작성자의 다른 글 조회
    List<CommunityDto> selectOtherPostsByUserId(
            @Param("userId") String userId,
            @Param("currentPostId") Long currentPostId
    );

    void viewCountIncrease (long id);

    void likeIncrease(@Param("id") long id, @Param("userId") String userId);
    void likeDecrease(@Param("id") long id, @Param("userId") String userId);

    int likeExists(@Param("id") long id, @Param("userId") String userId);

    int likeCount(@Param("id") long id);

    // ------------------- 게시글 관리 -------------------
    void insertPost(CommunityDto dto);

    void insertFile(@Param("targetId") Long targetId,
                    @Param("category") String category,
                    @Param("originalName") String originalName,
                    @Param("savedName") String savedName,
                    @Param("filePath") String filePath);

    void deletePost(Long id);

    // ------------------- 댓글 관리 -------------------
    List<CommentDto> selectCommentsByPostId(Long postId);
    void insertComment(CommentDto dto);
    void deleteComment(Long id);

    void deleteAllLike (Long id);
    void deleteAllComment(Long id);
}