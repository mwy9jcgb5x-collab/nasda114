package com.example.nasda.repository.manager;

import com.example.nasda.domain.ForbiddenWordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWordEntity, Integer> {
    boolean existsByWord(String word);
}