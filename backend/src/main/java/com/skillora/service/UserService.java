package com.skillora.service;

import com.skillora.dto.user.AddOfferedSkillRequest;
import com.skillora.dto.user.AddWantedSkillRequest;
import com.skillora.dto.user.UserResponse;
import com.skillora.dto.user.UserUpdateRequest;
import com.skillora.entity.*;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ConflictException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.mapper.UserMapper;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserOfferedSkillRepository offeredSkillRepository;
    private final UserWantedSkillRepository wantedSkillRepository;
    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final GamificationService gamificationService;

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserResponse getFullProfile(Long userId) {
        User user = getUserOrThrow(userId);
        List<UserOfferedSkill> offered = offeredSkillRepository.findByUserId(userId);
        List<UserWantedSkill> wanted = wantedSkillRepository.findByUserId(userId);

        UserResponse response = UserMapper.toResponseWithSkills(user, offered, wanted);
        response.setCompletedSessionsAsTeacher(sessionRepository.countCompletedAsTeacher(userId));
        response.setCompletedSessionsAsLearner(sessionRepository.countCompletedAsLearner(userId));
        response.setAverageRating(reviewRepository.averageRatingForUser(userId));
        response.setReviewCount(reviewRepository.countForUser(userId));
        return response;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> discover(String query, Long currentUserId, Pageable pageable) {
        Page<User> page = (query == null || query.isBlank())
                ? userRepository.findByActiveTrueAndIdNot(currentUserId, pageable)
                : userRepository.findByNameContainingIgnoreCaseAndActiveTrue(query, pageable);
        return page.map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = getUserOrThrow(userId);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone());
        if (request.getAvailability() != null) user.setAvailability(request.getAvailability());
        if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
        if (request.getExperienceLevel() != null) {
            try {
                user.setExperienceLevel(ProficiencyLevel.valueOf(request.getExperienceLevel().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid experienceLevel value");
            }
        }

        boolean wasComplete = user.getOnboardingComplete();
        if (!wasComplete && isProfileReasonablyComplete(user)) {
            user.setOnboardingComplete(true);
            gamificationService.awardPoints(user, GamificationService.XP_PROFILE_COMPLETE, "Completed profile");
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    private boolean isProfileReasonablyComplete(User user) {
        return user.getBio() != null && !user.getBio().isBlank()
                && user.getLocation() != null && !user.getLocation().isBlank();
    }

    @Transactional
    public UserResponse addOfferedSkill(Long userId, AddOfferedSkillRequest request) {
        User user = getUserOrThrow(userId);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (offeredSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
            throw new ConflictException("You already listed this skill as something you teach");
        }

        ProficiencyLevel level = ProficiencyLevel.BEGINNER;
        if (request.getProficiencyLevel() != null) {
            try {
                level = ProficiencyLevel.valueOf(request.getProficiencyLevel().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid proficiencyLevel value");
            }
        }

        UserOfferedSkill offered = UserOfferedSkill.builder()
                .user(user)
                .skill(skill)
                .proficiencyLevel(level)
                .yearsExperience(request.getYearsExperience() == null ? 0 : request.getYearsExperience())
                .build();
        offeredSkillRepository.save(offered);

        return getFullProfile(userId);
    }

    @Transactional
    public UserResponse addWantedSkill(Long userId, AddWantedSkillRequest request) {
        User user = getUserOrThrow(userId);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (wantedSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
            throw new ConflictException("You already listed this skill as something you want to learn");
        }

        ProficiencyLevel level = ProficiencyLevel.INTERMEDIATE;
        if (request.getTargetLevel() != null) {
            try {
                level = ProficiencyLevel.valueOf(request.getTargetLevel().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid targetLevel value");
            }
        }

        UserWantedSkill wanted = UserWantedSkill.builder()
                .user(user)
                .skill(skill)
                .targetLevel(level)
                .priority(request.getPriority() == null ? 1 : request.getPriority())
                .build();
        wantedSkillRepository.save(wanted);

        return getFullProfile(userId);
    }

    @Transactional
    public void removeOfferedSkill(Long userId, Long offeredSkillId) {
        offeredSkillRepository.deleteByIdAndUserId(offeredSkillId, userId);
    }

    @Transactional
    public void removeWantedSkill(Long userId, Long wantedSkillId) {
        wantedSkillRepository.deleteByIdAndUserId(wantedSkillId, userId);
    }
}
