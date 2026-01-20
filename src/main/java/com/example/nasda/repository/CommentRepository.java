package com.example.nasda.repository;

import com.example.nasda.domain.CommentEntity;
import com.example.nasda.domain.CommentReportEntity;
import com.example.nasda.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    // ✅ 최신 댓글이 위로 오게(created_at DESC)

    Page<CommentEntity> findByPost_PostIdOrderByCreatedAtDesc(Integer postId, Pageable pageable);
    Page<CommentEntity> findByUserId(Integer userId, Pageable pageable);

    List<CommentEntity> findByPost_PostIdOrderByCreatedAtDesc(Integer postId);
    long countByPost_PostId(Integer postId);

    void deleteByPost_PostId(Integer postId);

    long countByUserId(Integer userId);

    long countByPost_PostIdAndCreatedAtBefore(Integer postId, LocalDateTime createdAt);

    @Modifying
    @Query("delete from CommentEntity c where c.post.category.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
