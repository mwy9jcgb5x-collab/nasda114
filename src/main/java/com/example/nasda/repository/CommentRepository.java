package com.example.nasda.repository;

import com.example.nasda.domain.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    // ✅ 기존 게시글별 조회 (팀원 코드 유지)
    Page<CommentEntity> findByPost_PostIdOrderByCreatedAtDesc(Integer postId, Pageable pageable);
    List<CommentEntity> findByPost_PostIdOrderByCreatedAtDesc(Integer postId);
    long countByPost_PostId(Integer postId);
    void deleteByPost_PostId(Integer postId);

    // ✅ 1. 유저별 댓글 개수 (중복 제거됨)
    long countByUserId(Integer userId);

    // ✅ 2. 유저별 댓글 목록 조회
    Page<CommentEntity> findByUserId(Integer userId, Pageable pageable);

    // ✅ 3. 회원 탈퇴용: 작성자를 NULL로 변경 (Native Query)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE comments SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
    void setAuthorNull(@Param("userId") Integer userId);
}