package com.example.nasda.dto.manager;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Integer reportId;

    @NotEmpty

    private String reason;

    private String reporterName;

    private String status;

    private LocalDateTime createdAt;
}