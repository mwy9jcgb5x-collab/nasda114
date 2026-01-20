package com.example.nasda.dto.manager;

import com.example.nasda.domain.ProcessResult;
import com.example.nasda.domain.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostReportDTO {
    private Integer reportId;
    private String reason;
    private ReportStatus status;
    private String adminComment;
    private ProcessResult processResult;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}