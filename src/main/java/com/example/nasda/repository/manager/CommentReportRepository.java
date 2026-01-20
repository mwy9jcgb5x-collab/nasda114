package com.example.nasda.repository.manager;

import com.example.nasda.domain.CommentReportEntity;
import com.example.nasda.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReportEntity, Integer> {
    List<CommentReportEntity> findByStatus(ReportStatus status);

}