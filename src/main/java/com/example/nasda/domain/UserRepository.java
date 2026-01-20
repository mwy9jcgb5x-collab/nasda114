package com.example.nasda.domain;



import com.example.nasda.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // 🔹 이미지 컬럼명(login_id, nickname, email)에 맞춘 중복 확인 메서드
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByEmail(String email); // 이메일로 사용자 정보 가져오기
    Optional<UserEntity> findByLoginIdAndEmail(String loginId, String email);

    @Query(value = "SELECT user_id, nickname, status, suspension_end_date FROM users", nativeQuery = true)
    List<Map<String, Object>> findAllUserStatusRaw();
}
