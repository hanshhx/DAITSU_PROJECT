package com.example.TEAM202507_01.menus.community.repository;

import com.example.TEAM202507_01.menus.community.dto.CommentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper // ★ 이게 있어야 오토와이어링(Bean 등록)이 됩니다.
public interface CommentMapper {

    List<CommentDto> findAllByPostId(Long postId);
    // DB에 있는 모든 글을 가져와서 리스트로 만듦. (목록 조회)

    // 댓글 저장
    void save(CommentDto dto);

    // 댓글 수정
    void update(CommentDto dto);

    // 댓글 삭제
    void delete(Long id);

    // (선택) 게시글 삭제 시 관련 댓글 일괄 삭제가 필요하다면
    void deleteAllByPostId(Long postId);
}