package com.example.TEAM202507_01.menus.community.controller;

import com.example.TEAM202507_01.alramo.service.AlramoService;
import com.example.TEAM202507_01.menus.community.dto.CommentDto;
import com.example.TEAM202507_01.menus.community.dto.CommunityDto;
import com.example.TEAM202507_01.menus.community.service.CommunityService;
import com.example.TEAM202507_01.user.dto.UserDto;
import com.example.TEAM202507_01.user.service.FavoriteService;
import com.example.TEAM202507_01.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final AlramoService alramoService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    @Value("${server.redirect.address}")
    private String serverUrl;

    // ==========================================
    // 🚑 1. [긴급 패치] 레거시 경로 지원 (프론트 호환용)
    // ==========================================

    @PostMapping(value = "/free", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Long> createFreePostLegacy(
            @RequestPart("dto") CommunityDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        log.info("🚑 [Legacy Support] POST /free 요청 감지 - 처리 시작");
        dto.setCategory("FREE");
        long postId = communityService.savePost(dto, files);
//        TODO 서비스 시 고쳐야함
        String postLink = "http://"+serverUrl+"/community/free/" + postId;
        try { alramoService.sendNewPostNotification(dto.getTitle(), postLink);} catch (Exception e) {}
        return ResponseEntity.ok(postId);
    }

    // [수정] URL 경로: /recommend -> /notice
    // [수정] 메서드명: createRecommendPostLegacy -> createNoticePostLegacy
    @PostMapping(value = "/notice", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Long> createNoticePostLegacy(
            @RequestPart("dto") CommunityDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        // [수정] 카테고리 설정: RECOMMEND -> NOTICE
        dto.setCategory("NOTICE");
        long postId = communityService.savePost(dto, files);
        return ResponseEntity.ok(postId);
    }

    // ==========================================
    // 📢 2. 게시글 조회 (Read)
    // ==========================================

    @GetMapping("/free")
    public ResponseEntity<List<CommunityDto>> getFreeBoardList() {
        return ResponseEntity.ok(communityService.getPostList("FREE", 1, 100));
    }

    // [수정] URL 경로: /recommend -> /notice
    // [수정] 메서드명: getRecommendBoardList -> getNoticeBoardList
    @GetMapping("/notice")
    public ResponseEntity<List<CommunityDto>> getNoticeBoardList() {
        // [수정] 조회 파라미터: RECOMMEND -> NOTICE
        return ResponseEntity.ok(communityService.getPostList("NOTICE", 1, 100));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityDto>> getPostList(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.getPostList(category, page, size));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CommunityDto>> getPostsByCategoryPath(
            @PathVariable String category,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityService.getPostList(category.toUpperCase(), page, size));
    }

    // [수정] 상세 조회 시 로그인 유저 정보(user)를 받아서 서비스로 전달
    @GetMapping("/post/{id:[0-9]+}")
    public ResponseEntity<CommunityDto> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user // 👈 추가: 로그인 정보 확인
    ) {
        String currentUserId = (user != null) ? user.getUsername() : null;
        // Service에 userId도 같이 넘김
        return ResponseEntity.ok(communityService.findPostById(id, currentUserId));
    }

    @GetMapping("/free/{id:[0-9]+}")
    public ResponseEntity<CommunityDto> getFreePostDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        String currentUserId = (user != null) ? user.getUsername() : null;
        // Service에 userId도 같이 넘김
        return ResponseEntity.ok(communityService.findPostById(id, currentUserId));
    }

    @DeleteMapping("/free/{id:[0-9]+}")
    public ResponseEntity<String> deleteFreePost(@PathVariable Long id) {
        communityService.deleteComment(id);
        communityService.deletePost(id);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }


    // ... (이하 나머지 코드는 파일 관련, 좋아요, 댓글 기능으로 'recommend' 문자열이 없으므로 그대로 유지) ...

    @GetMapping("/post/{id:[0-9]+}/files")
    public ResponseEntity<List<String>> getPostFiles(@PathVariable Long id) {
        log.info("🖼️ 게시글 {} 번의 파일 목록 조회 요청", id);
        return ResponseEntity.ok(communityService.getFilePathsByPostId(id));
    }

    @GetMapping("/user/{userId}/others")
    public ResponseEntity<List<CommunityDto>> getOtherPosts(
            @PathVariable String userId,
            @RequestParam("currentPostId") Long currentPostId
    ) {
        return ResponseEntity.ok(communityService.getOtherPostsByUser(userId, currentPostId));
    }

    @PostMapping("/image-upload")
    public ResponseEntity<String> uploadEditorImage(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(communityService.uploadEditorImage(file));
    }

    @PostMapping(value = "/post", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Long> createPost(
            @RequestPart("dto") CommunityDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        long postId = communityService.savePost(dto, files);
        String postLink = "http://"+serverUrl+"/community/review/" + postId;
        try { alramoService.sendNewPostNotification(dto.getTitle(), postLink); } catch (Exception e) {}
        return ResponseEntity.ok(postId);
    }

    @DeleteMapping("/post/{id:[0-9]+}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        communityService.deleteAllLike(id);
        communityService.deleteComment(id);
        communityService.deletePost(id);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    @GetMapping("/comments/{postId:[0-9]+}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.findCommentsByPostId(postId));
    }

    @PostMapping("/comments")
    public ResponseEntity<String> saveComment(@RequestBody CommentDto dto) {
        communityService.saveComment(dto);
        return ResponseEntity.ok("댓글이 등록되었습니다.");
    }

    @PostMapping("/comments/delete")
    public ResponseEntity<String> deleteComment(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id != null) communityService.deleteComment(id);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }

    @PostMapping("/post/{id:[0-9]+}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        favoriteService.toggleFavorite("COMMUNITY", user.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기 처리 완료"));
    }

    @PostMapping("/post/{id}/like")
    public ResponseEntity<Object> likeIncrease(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        UserDto userDto = userService.findById(user.getUsername());
        String userId = userDto.getId(); // (참고: 아까 UUID 변환 로직 넣었으면 그거 사용)

        // 1. 좋아요 토글 수행
        communityService.likeIncrease(id, userId);

        // 2. 🔥 [핵심] 최신 좋아요 개수를 다시 조회
        int newCount = communityService.likeCount(id);

        // 3. JSON 형태로 반환 (likeCount 필드 포함)
        return ResponseEntity.ok(Map.of(
                "likeCount", newCount
        ));
    }

    @GetMapping("/post/{id}/likecount")
    public ResponseEntity<Integer> likeCount(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.likeCount(id));
    }

    @GetMapping("/post/{id}/isuserliked")
    public ResponseEntity<Boolean> isUserLiked(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        UserDto userDto = userService.findById(user.getUsername());
        String userId = userDto.getId();
        return ResponseEntity.ok(communityService.isUserLiked(id, userId));
    }
}