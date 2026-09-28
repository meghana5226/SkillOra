package com.skillora.service;

import com.skillora.dto.goal.LearningGoalRequest;
import com.skillora.dto.goal.LearningGoalResponse;
import com.skillora.entity.GoalStatus;
import com.skillora.entity.LearningGoal;
import com.skillora.entity.Skill;
import com.skillora.entity.User;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.repository.LearningGoalRepository;
import com.skillora.repository.SkillRepository;
import com.skillora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningGoalService {

    private final LearningGoalRepository learningGoalRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LearningGoalResponse> list(Long userId) {
        return learningGoalRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public LearningGoalResponse create(Long userId, LearningGoalRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Skill skill = null;
        if (request.getSkillId() != null) {
            skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        }

        LearningGoal goal = LearningGoal.builder()
                .user(user)
                .title(request.getTitle().trim())
                .skill(skill)
                .targetDate(request.getTargetDate())
                .progress(request.getProgress() == null ? 0 : clampProgress(request.getProgress()))
                .status(GoalStatus.IN_PROGRESS)
                .build();

        return toResponse(learningGoalRepository.save(goal));
    }

    @Transactional
    public LearningGoalResponse update(Long userId, Long goalId, LearningGoalRequest request) {
        LearningGoal goal = learningGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) goal.setTitle(request.getTitle().trim());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());
        if (request.getProgress() != null) goal.setProgress(clampProgress(request.getProgress()));
        if (request.getStatus() != null) {
            try {
                goal.setStatus(GoalStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid status value");
            }
        }
        if (goal.getProgress() >= 100) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        return toResponse(learningGoalRepository.save(goal));
    }

    private int clampProgress(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private LearningGoalResponse toResponse(LearningGoal g) {
        return LearningGoalResponse.builder()
                .id(g.getId())
                .title(g.getTitle())
                .skillId(g.getSkill() == null ? null : g.getSkill().getId())
                .skillName(g.getSkill() == null ? null : g.getSkill().getName())
                .targetDate(g.getTargetDate())
                .progress(g.getProgress())
                .status(g.getStatus().name())
                .build();
    }
}
