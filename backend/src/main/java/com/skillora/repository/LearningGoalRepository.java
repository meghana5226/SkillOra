package com.skillora.repository;

import com.skillora.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {
    List<LearningGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<LearningGoal> findByIdAndUserId(Long id, Long userId);
}
