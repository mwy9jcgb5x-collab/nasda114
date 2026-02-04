package com.example.nasda.repository.manager;

import com.example.nasda.domain.CommentReportEntity;
import com.example.nasda.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReportEntity, Integer> {

    // ✅ [필수] 댓글 신고 승인 시 '딱 해당 댓글 신고'만 지우는 메서드 (주석 풀고 추가)
    @Modifying
    @Transactional
    @Query("DELETE FROM CommentReportEntity r WHERE r.comment.commentId = :commentId")
    void deleteByCommentId(@Param("commentId") Integer commentId);

    // ✅ 게시글 삭제 시, 그 게시글에 달린 댓글들의 신고 내역을 광속으로 지우는 메서드
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment_reports WHERE comment_id IN (SELECT comment_id FROM comments WHERE post_id = :postId)", nativeQuery = true)
    void deleteByPostIdNative(@Param("postId") Integer postId);

    Page<CommentReportEntity> findByStatus(ReportStatus status, Pageable pageable);
    List<CommentReportEntity> findByStatus(ReportStatus status);
}