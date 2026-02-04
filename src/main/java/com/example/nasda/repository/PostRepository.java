package com.example.nasda.repository;

import com.example.nasda.domain.PostEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    // ✅ 캘린더 이미지 조회를 위해 추가된 메서드
    List<PostEntity> findAllByUser_UserId(Integer userId);

    long countByUser_UserId(Integer userId);

    // ✅ 내 전체 포스트 목록 조회
    List<PostEntity> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    List<PostEntity> findTop4ByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    List<PostEntity> findAllByOrderByCreatedAtDesc();

    List<PostEntity> findTop30ByOrderByCreatedAtDesc();

    // ✅ 카테고리 필터 + 페이징
    Page<PostEntity> findByCategory_CategoryNameOrderByCreatedAtDesc(String categoryName, Pageable pageable);

    // ✅ 전체 + 페이징
    Page<PostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        select p
        from PostEntity p
        join fetch p.user
        join fetch p.category
        order by p.createdAt desc
    """)
    List<PostEntity> findAllWithUserAndCategoryOrderByCreatedAtDesc();

    // =========================
    // ✅ 검색 기능용
    // =========================
    List<PostEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<PostEntity> findByDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<PostEntity> findByUser_NicknameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<PostEntity> findByCategory_CategoryNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    Page<PostEntity> findByUser_UserId(Integer userId, Pageable pageable);

    // ✅ 61행 수정 완료: p.user.id가 아니라 p.user.userId여야 합니다.
    @Modifying
    @Transactional
// p.user = NULL 대신 p.user.userId = 0으로 수정
    @Query("UPDATE PostEntity p SET p.user.userId = 0 WHERE p.user.userId = :userId")
    void setAuthorNull(@Param("userId") Integer userId);

    @Modifying
    @Query("delete from PostImageEntity pi where pi.post.category.categoryId = :categoryId")
    void deletePostImagesByCategoryId(@Param("categoryId") Integer categoryId);

    @Modifying
    @Query("delete from PostEntity p where p.category.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("""

            select p from PostEntity p
    where lower(p.title) like lower(concat('%', :keyword, '%'))
       or lower(p.description) like lower(concat('%', :keyword, '%'))
    order by p.createdAt desc
    """)
    List<PostEntity> searchTitleOrDescription(@Param("keyword") String keyword);

    // 수정 전: @Transactional (jakarta...) 가 붙어있음
    // 수정 후: 아래처럼 @Modifying 옵션을 추가하고 Transactional은 제거하세요.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.transaction.annotation.Transactional // 스프링 트랜잭션 사용
    @Query("delete from PostImageEntity pi where pi.post.postId = :postId")
    void deletePostImagesByPostId(@Param("postId") Integer postId);
    }