package com.skillora.mapper;

import com.skillora.dto.skill.SkillResponse;
import com.skillora.dto.user.OfferedSkillResponse;
import com.skillora.dto.user.UserResponse;
import com.skillora.dto.user.WantedSkillResponse;
import com.skillora.entity.Skill;
import com.skillora.entity.User;
import com.skillora.entity.UserOfferedSkill;
import com.skillora.entity.UserWantedSkill;

import java.util.List;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .location(user.getLocation())
                .timezone(user.getTimezone())
                .experienceLevel(user.getExperienceLevel().name())
                .availability(user.getAvailability())
                .role(user.getRole().name())
                .points(user.getPoints())
                .level(user.getLevel())
                .reputationScore(user.getReputationScore())
                .onboardingComplete(user.getOnboardingComplete())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse toResponseWithSkills(User user, List<UserOfferedSkill> offered, List<UserWantedSkill> wanted) {
        UserResponse response = toResponse(user);
        response.setOfferedSkills(offered.stream().map(UserMapper::toOfferedResponse).toList());
        response.setWantedSkills(wanted.stream().map(UserMapper::toWantedResponse).toList());
        return response;
    }

    public static OfferedSkillResponse toOfferedResponse(UserOfferedSkill os) {
        return OfferedSkillResponse.builder()
                .id(os.getId())
                .skillId(os.getSkill().getId())
                .skillName(os.getSkill().getName())
                .category(os.getSkill().getCategory())
                .proficiencyLevel(os.getProficiencyLevel().name())
                .yearsExperience(os.getYearsExperience())
                .build();
    }

    public static WantedSkillResponse toWantedResponse(UserWantedSkill ws) {
        return WantedSkillResponse.builder()
                .id(ws.getId())
                .skillId(ws.getSkill().getId())
                .skillName(ws.getSkill().getName())
                .category(ws.getSkill().getCategory())
                .targetLevel(ws.getTargetLevel().name())
                .priority(ws.getPriority())
                .build();
    }

    public static SkillResponse toSkillResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .build();
    }

    private UserMapper() {}
}
