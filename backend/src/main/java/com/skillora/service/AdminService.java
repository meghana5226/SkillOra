package com.skillora.service;

import com.skillora.dto.admin.AdminDashboardResponse;
import com.skillora.dto.user.UserResponse;
import com.skillora.entity.AdminAudit;
import com.skillora.entity.SessionStatus;
import com.skillora.entity.SwapStatus;
import com.skillora.entity.User;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.mapper.UserMapper;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SwapRequestRepository swapRequestRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;
    private final AdminAuditRepository adminAuditRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long totalSkills = skillRepository.count();
        long acceptedSwaps = swapRequestRepository.countByStatus(SwapStatus.ACCEPTED);
        long pendingSwaps = swapRequestRepository.countByStatus(SwapStatus.PENDING);
        long completedSessions = sessionRepository.countByStatus(SessionStatus.COMPLETED);

        Double avgRatingRaw = reviewRepository.count() == 0 ? 0.0 :
                userRepository.findAll().stream()
                        .map(u -> reviewRepository.averageRatingForUser(u.getId()))
                        .filter(java.util.Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .average().orElse(0.0);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalSkills(totalSkills)
                .activeSwaps(acceptedSwaps)
                .pendingSwaps(pendingSwaps)
                .completedSessions(completedSessions)
                .averageRating(Math.round(avgRatingRaw * 100.0) / 100.0)
                .recentRegistrations7d(userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7)))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse setUserActive(Long adminId, Long targetUserId, boolean active) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        target.setActive(active);
        userRepository.save(target);

        User admin = userRepository.findById(adminId).orElseThrow();
        adminAuditRepository.save(AdminAudit.builder()
                .admin(admin)
                .action(active ? "ACTIVATE_USER" : "DEACTIVATE_USER")
                .targetType("USER")
                .targetId(targetUserId)
                .details("Set active=" + active)
                .build());

        return UserMapper.toResponse(target);
    }
}
