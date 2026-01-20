package com.example.nasda.repository.manager;

import com.example.nasda.domain.PostReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReportEntity, Integer> {
    // 해당 카테고리에 속한 글들의 '신고 기록'을 먼저 삭제
    @Modifying
    @Query("delete from PostReportEntity r where r.post.category.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Integer categoryId);
}