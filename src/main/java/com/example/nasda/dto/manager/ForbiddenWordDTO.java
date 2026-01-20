package com.example.nasda.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForbiddenWordDTO {
    private Integer  forbiddenwordId; // fno 대신 id로 변경
    private String word;
}
