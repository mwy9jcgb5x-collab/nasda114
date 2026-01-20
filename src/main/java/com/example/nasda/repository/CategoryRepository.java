package com.example.nasda.repository;

import com.example.nasda.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    Optional<CategoryEntity> findByCategoryName(String categoryName);

    // ✅ 중복 체크를 위해 이 메서드가 반드시 필요합니다!
    boolean existsByCategoryName(String categoryName);
    // ✅ 이 한 줄이 없으면 Impl에서 백날 수정해도 빨간 줄 안 사라집니다!
    void deleteByCategoryName(String categoryName);
}
