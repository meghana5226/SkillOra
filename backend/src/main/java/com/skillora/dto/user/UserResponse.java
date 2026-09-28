package com.skillora.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String profileImage;
    private String bio;
    private String location;
    private String timezone;
    private String experienceLevel;
    private String availability;
    private String role;
    private Integer points;
    private Integer level;
    private BigDecimal reputationScore;
    private Boolean onboardingComplete;
    private Boolean active;
    private LocalDateTime createdAt;

    private List<OfferedSkillResponse> offeredSkills;
    private List<WantedSkillResponse> wantedSkills;

    // aggregate stats, populated only when explicitly requested
    private Long completedSessionsAsTeacher;
    private Long completedSessionsAsLearner;
    private Double averageRating;
    private Long reviewCount;
}
