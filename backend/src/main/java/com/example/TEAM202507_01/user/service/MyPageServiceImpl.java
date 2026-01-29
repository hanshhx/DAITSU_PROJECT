package com.example.TEAM202507_01.user.service;

import com.example.TEAM202507_01.user.dto.MyPageDto;
import com.example.TEAM202507_01.user.repository.MyPageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final MyPageMapper myPageMapper;

    // LoginID로 UUID를 찾는 메서드 (없으면 LoginID 반환)
    private String getUuid(String loginId) {
        String uuid = myPageMapper.findUuidByLoginId(loginId);
        if (uuid == null) {
            log.warn("⚠️ UUID 조회 실패! DB에 해당 loginId({})가 없거나 ID 컬럼이 비어있습니다. loginId로 대신 조회합니다.", loginId);
            return loginId;
        }
        return uuid;
    }

    @Override
    public MyPageDto getMyInfo(String loginId) {
        return myPageMapper.selectUserInfo(loginId);
    }

    @Override
    public List<Map<String, Object>> getMyPosts(String loginId, int offset, int size) {
        String uuid = getUuid(loginId);
        return myPageMapper.selectMyPosts(uuid, offset + 1, offset + size); // startRow, endRow 계산
    }

    @Override
    public List<Map<String, Object>> getMyComments(String loginId, int offset, int size) {
        String uuid = getUuid(loginId);
        return myPageMapper.selectMyComments(uuid, offset + 1, offset + size);
    }

    @Override
    public List<Map<String, Object>> getMyFavorites(String loginId, int offset, int size) {
        String uuid = getUuid(loginId);
        return myPageMapper.selectMyFavorites(uuid, offset + 1, offset + size);
    }

    @Override
    public void updateMyInfo(MyPageDto dto) {
        myPageMapper.updateUserInfo(dto);
    }

    @Override
    public void updatePost(Long id, String loginId, String title, String content) {
        myPageMapper.updatePost(id, getUuid(loginId), title, content);
    }

    @Override
    public void updateComment(Long id, String loginId, String content) {
        myPageMapper.updateComment(id, getUuid(loginId), content);
    }

    @Override
    public void deletePost(Long id, String loginId) {
        myPageMapper.deletePost(id, getUuid(loginId));
    }

    @Override
    public void deleteComment(Long id, String loginId) {
        myPageMapper.deleteComment(id, getUuid(loginId));
    }

    @Override
    public void deleteFavorite(Long id, String loginId) {
        myPageMapper.deleteFavorite(id, getUuid(loginId));
    }
}