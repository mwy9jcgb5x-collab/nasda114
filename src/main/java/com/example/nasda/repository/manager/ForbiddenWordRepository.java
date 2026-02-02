package com.example.nasda.repository.manager;

import com.example.nasda.domain.ForbiddenWordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWordEntity, Integer> {
    boolean existsByWord(String word);

    // 🔍 금지어 검색 기능 추가 (word 필드에서 검색)
    Page<ForbiddenWordEntity> findByWordContaining(String word, Pageable pageable);
}