package com.skillora.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddWantedSkillRequest {

    @NotNull(message = "skillId is required")
    private Long skillId;

    private String targetLevel;

    private Integer priority;
}
