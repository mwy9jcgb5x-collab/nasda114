package com.example.nasda.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;

    private String username;

    private String suspensionReason;

    private LocalDateTime suspensionStartDate;

    private LocalDateTime suspensionEndDate;
}