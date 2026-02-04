package com.example.nasda.repository.manager;

import com.example.nasda.domain.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    @Modifying
    @Transactional
    // 엔티티에 postId 필드가 없으므로, 메시지 내용에 게시글 번호가 포함된 알림을 찾아 지웁니다.
    @Query("DELETE FROM NotificationEntity n WHERE n.message LIKE %:postId%")
    void deleteByPostIdCustom(@Param("postId") Integer postId);
}