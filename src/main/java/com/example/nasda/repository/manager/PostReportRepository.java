package com.example.nasda.repository.manager;

import com.example.nasda.domain.PostEntity;
import com.example.nasda.domain.PostReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostReportRepository extends JpaRepository<PostReportEntity, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PostReportEntity r WHERE r.post.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostReportEntity r WHERE r.post.category.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Integer categoryId);

    void deleteByPost(PostEntity post);
}