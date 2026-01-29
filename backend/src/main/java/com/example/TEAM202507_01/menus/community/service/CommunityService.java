package com.example.TEAM202507_01.menus.community.service;

import com.example.TEAM202507_01.menus.community.dto.CommentDto;
import com.example.TEAM202507_01.menus.community.dto.CommunityDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CommunityService {
    List<CommunityDto> getPostList(String category, int page, int size);
    List<CommunityDto> findPostsByCategory(String category);
    CommunityDto findPostById(Long id, String userId);
    List<CommunityDto> getOtherPostsByUser(String userId, Long currentPostId);
    String uploadEditorImage(MultipartFile file);
    long savePost(CommunityDto dto, List<MultipartFile> files);
    void deletePost(Long id);

    // ✨ [추가] 컨트롤러에서 호출하는 메서드 정의
    List<String> getFilePathsByPostId(Long postId);

    // 댓글 관련
    List<CommentDto> findCommentsByPostId(Long postId);
    void saveComment(CommentDto dto);
    void deleteComment(Long id);


    void likeIncrease(Long id, String userId);
    int likeCount(Long id);

    boolean isUserLiked(Long id, String userId);

    void deleteAllLike(Long id);
    void deleteAllComment(Long id);
}