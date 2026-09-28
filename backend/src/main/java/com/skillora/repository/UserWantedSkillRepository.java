package com.skillora.repository;

import com.skillora.entity.UserWantedSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWantedSkillRepository extends JpaRepository<UserWantedSkill, Long> {
    List<UserWantedSkill> findByUserId(Long userId);
    Optional<UserWantedSkill> findByUserIdAndSkillId(Long userId, Long skillId);
    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
    void deleteByIdAndUserId(Long id, Long userId);
}
