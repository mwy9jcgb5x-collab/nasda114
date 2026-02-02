package com.example.nasda.service;

import com.example.nasda.domain.*;
import com.example.nasda.dto.comment.CommentViewDto;
import com.example.nasda.repository.CommentRepository;
import com.example.nasda.repository.PostRepository;
import com.example.nasda.repository.manager.CommentReportRepository; // 🚩 추가됨
import com.example.nasda.service.manager.AdminService; // 🚩 추가됨
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // 🚩 추가됨
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AdminService adminService; // 🚩 관리자 서비스 주입
    private final CommentReportRepository commentReportRepository; // 🚩 신고 레포지토리 주입
    private final UserRepository userRepository; // 🚩 유저 레포지토리 주입

    public Page<CommentViewDto> getCommentsPage(Integer postId, int page, int size, Integer currentUserId) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);

        return commentRepository
                .findByPost_PostIdOrderByCreatedAtDesc(postId, PageRequest.of(safePage, safeSize))
                .map(e -> new CommentViewDto(
                        e.getCommentId(),
                        e.getContent(),
                        "사용자" + e.getUserId(),
                        e.getCreatedAt(),
                        currentUserId != null && e.getUserId().equals(currentUserId)
                ));
    }

    @Transactional
    public Integer createComment(Integer postId, Integer userId, String content) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글: " + postId));

        String trimmed = content == null ? "" : content.trim();

        // 🚩 [관리자 금지어 체크 추가]
        if (adminService.checkForbiddenWords(trimmed)) {
            throw new IllegalArgumentException("금지어가 포함된 댓글은 등록할 수 없습니다.");
        }

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        }
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("댓글은 최대 500자까지 가능합니다.");
        }

        // CommentEntity.create() 팩토리 메서드 사용
        CommentEntity c = CommentEntity.create(post, userId, trimmed);
        CommentEntity saved = commentRepository.save(c);
        return saved.getCommentId();
    }

    public int getLastPageIndex(Integer postId, int size) {
        int safeSize = Math.max(1, size);
        long total = commentRepository.countByPost_PostId(postId);

        if (total <= 0) return 0;

        return (int) ((total - 1) / safeSize);
    }

    @Transactional
    public Integer deleteComment(Integer commentId, Integer currentUserId) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        if (!comment.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인 댓글만 삭제할 수 있습니다.");
        }

        Integer postId = comment.getPost().getPostId();
        commentRepository.delete(comment);
        return postId;
    }

    @Transactional
    public Integer editComment(Integer commentId, Integer currentUserId, String newContent) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        if (!comment.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인 댓글만 수정할 수 있습니다.");
        }

        String trimmed = newContent == null ? "" : newContent.trim();

        // 🚩 [관리자 금지어 체크 추가] 수정 시에도 검사
        if (adminService.checkForbiddenWords(trimmed)) {
            throw new IllegalArgumentException("금지어가 포함된 댓글로 수정할 수 없습니다.");
        }

        if (trimmed.isEmpty()) throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        if (trimmed.length() > 500) throw new IllegalArgumentException("댓글은 최대 500자까지 가능합니다.");

        comment.edit(trimmed);
        return comment.getPost().getPostId();
    }

    @Transactional(readOnly = true)
    public Page<CommentEntity> findByUserId(Integer userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public int getPageNumberByCommentId(Integer postId, Integer commentId, int pageSize) {
        List<CommentEntity> allComments = commentRepository.findByPost_PostIdOrderByCreatedAtDesc(postId);

        int index = 0;
        for (int i = 0; i < allComments.size(); i++) {
            if (allComments.get(i).getCommentId().equals(commentId)) {
                index = i;
                break;
            }
        }
        return index / pageSize;
    }

    // 🚩 [댓글 신고 로직 추가] 관리자 기능 연동
    @Transactional
    public void reportComment(Integer commentId, Integer userId, String reason) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        UserEntity reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        CommentReportEntity report = CommentReportEntity.builder()
                .comment(comment)
                .reporter(reporter) // 엔티티 필드명에 맞게 설정 (보통 reporter)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .status(ReportStatus.PENDING) // 처리 대기 상태
                .build();

        commentReportRepository.save(report);
    }
}