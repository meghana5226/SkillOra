package com.skillora.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddOfferedSkillRequest {

    @NotNull(message = "skillId is required")
    private Long skillId;

    private String proficiencyLevel;

    @Min(value = 0, message = "yearsExperience cannot be negative")
    private Integer yearsExperience;
}
