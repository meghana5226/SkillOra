package com.skillora.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningGoalResponse {
    private Long id;
    private String title;
    private Long skillId;
    private String skillName;
    private LocalDate targetDate;
    private Integer progress;
    private String status;
}
