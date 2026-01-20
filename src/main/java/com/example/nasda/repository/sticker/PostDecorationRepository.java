package com.example.nasda.repository.sticker;

import com.example.nasda.domain.PostDecorationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostDecorationRepository extends JpaRepository<PostDecorationEntity, Integer> {

    // ✅ 도배 방지용 카운트
    long countByUser_UserIdAndPostImage_ImageId(Integer userId, Integer imageId);

    // ✅ 목록 조회: 이미지별 스티커 리스트 (Sticker 정보 Fetch Join)
    @EntityGraph(attributePaths = {"sticker"})
    List<PostDecorationEntity> findByPostImage_ImageId(Integer imageId);

    // ✅ 게시글 전체 조회용
    List<PostDecorationEntity> findByPostPostId(Integer postId);

    // ✅ [데드락 해결 핵심] 벌크 삭제: 수정/삭제 시 기존 데이터를 한 번에 날립니다.
    @Modifying
    @Transactional
    @Query("DELETE FROM PostDecorationEntity d WHERE d.user.userId = :userId AND d.postImage.imageId = :imageId")
    void deleteByUserAndImageBulk(@Param("userId") Integer userId, @Param("imageId") Integer imageId);

    // ✅ 게시글/이미지 삭제 시 연쇄 삭제용
    @Modifying
    @Transactional
    void deleteByPostPostId(Integer postId);

    @Modifying
    @Transactional
    void deleteByPostImageImageId(Integer imageId);

    @Modifying
    @Transactional
    @Query("UPDATE PostDecorationEntity d SET d.posX = :posX, d.posY = :posY WHERE d.decorationId = :id")
    void updatePosition(@Param("id") Integer id, @Param("posX") float posX, @Param("posY") float posY);
}