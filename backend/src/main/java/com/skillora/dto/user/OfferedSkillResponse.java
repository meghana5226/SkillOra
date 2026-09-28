package com.skillora.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferedSkillResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String category;
    private String proficiencyLevel;
    private Integer yearsExperience;
}
