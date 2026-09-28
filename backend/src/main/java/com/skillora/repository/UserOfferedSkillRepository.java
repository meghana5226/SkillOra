package com.skillora.repository;

import com.skillora.entity.UserOfferedSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserOfferedSkillRepository extends JpaRepository<UserOfferedSkill, Long> {
    List<UserOfferedSkill> findByUserId(Long userId);
    Optional<UserOfferedSkill> findByUserIdAndSkillId(Long userId, Long skillId);
    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
    void deleteByIdAndUserId(Long id, Long userId);
}
