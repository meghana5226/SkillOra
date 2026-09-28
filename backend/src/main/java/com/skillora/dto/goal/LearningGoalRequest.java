package com.skillora.dto.goal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LearningGoalRequest {

    @NotBlank(message = "title is required")
    private String title;

    private Long skillId;

    private LocalDate targetDate;

    private Integer progress;

    private String status;
}
