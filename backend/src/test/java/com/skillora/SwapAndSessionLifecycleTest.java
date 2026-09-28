package com.skillora;

import com.skillora.dto.review.ReviewCreateRequest;
import com.skillora.dto.session.SessionCreateRequest;
import com.skillora.dto.session.SessionResponse;
import com.skillora.dto.swap.SwapRequestCreateRequest;
import com.skillora.dto.swap.SwapRequestResponse;
import com.skillora.entity.*;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ConflictException;
import com.skillora.repository.SkillRepository;
import com.skillora.repository.UserRepository;
import com.skillora.service.ReviewService;
import com.skillora.service.SessionService;
import com.skillora.service.SwapRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SwapAndSessionLifecycleTest {

    @Autowired private UserRepository userRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private SwapRequestService swapRequestService;
    @Autowired private SessionService sessionService;
    @Autowired private ReviewService reviewService;

    @Test
    void fullLifecycle_swapAcceptSessionCompleteAndReview_persistsAndAwardsXp() {
        User teacher = userRepository.save(baseUser("teacher-" + System.nanoTime() + "@test.com"));
        User learner = userRepository.save(baseUser("learner-" + System.nanoTime() + "@test.com"));
        Skill skill = skillRepository.save(Skill.builder().name("Docker-T2").category("Programming").build());
        Skill counterSkill = skillRepository.save(Skill.builder().name("Kubernetes-T2").category("Programming").build());

        SwapRequestCreateRequest swapReq = new SwapRequestCreateRequest();
        swapReq.setReceiverId(teacher.getId());
        swapReq.setOfferedSkillId(counterSkill.getId());
        swapReq.setRequestedSkillId(skill.getId());
        swapReq.setMessage("Trade?");
        SwapRequestResponse swap = swapRequestService.create(learner.getId(), swapReq);
        assertThat(swap.getStatus()).isEqualTo("PENDING");

        SwapRequestResponse accepted = swapRequestService.accept(teacher.getId(), swap.getId());
        assertThat(accepted.getStatus()).isEqualTo("ACCEPTED");

        // Cannot accept twice
        assertThatThrownBy(() -> swapRequestService.accept(teacher.getId(), swap.getId()))
                .isInstanceOf(BadRequestException.class);

        SessionCreateRequest sessionReq = new SessionCreateRequest();
        sessionReq.setReceiverId(teacher.getId());
        sessionReq.setSkillId(skill.getId());
        sessionReq.setScheduledDate(LocalDate.now().plusDays(1));
        sessionReq.setStartTime(LocalTime.of(10, 0));
        sessionReq.setEndTime(LocalTime.of(11, 0));
        SessionResponse session = sessionService.create(learner.getId(), sessionReq);
        assertThat(session.getStatus()).isEqualTo("REQUESTED");

        // Cannot complete before confirming
        assertThatThrownBy(() -> sessionService.complete(learner.getId(), session.getId()))
                .isInstanceOf(BadRequestException.class);

        sessionService.confirm(teacher.getId(), session.getId());
        SessionResponse completed = sessionService.complete(learner.getId(), session.getId());
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");

        int teacherPointsBefore = userRepository.findById(teacher.getId()).orElseThrow().getPoints();
        assertThat(teacherPointsBefore).isGreaterThanOrEqualTo(50); // +50 XP for teaching

        ReviewCreateRequest reviewReq = new ReviewCreateRequest();
        reviewReq.setSessionId(session.getId());
        reviewReq.setRating(5);
        reviewReq.setComment("Excellent session!");
        reviewService.create(learner.getId(), reviewReq);

        User teacherAfterReview = userRepository.findById(teacher.getId()).orElseThrow();
        assertThat(teacherAfterReview.getReputationScore()).isEqualByComparingTo(BigDecimal.valueOf(5.0).setScale(2));
        assertThat(teacherAfterReview.getPoints()).isGreaterThanOrEqualTo(75); // +50 session, +25 five-star review

        // Duplicate review by the same reviewer must be rejected
        assertThatThrownBy(() -> reviewService.create(learner.getId(), reviewReq))
                .isInstanceOf(ConflictException.class);
    }

    private User baseUser(String email) {
        return User.builder()
                .name("Lifecycle Test " + email)
                .email(email)
                .password("hashed")
                .role(Role.USER)
                .experienceLevel(ProficiencyLevel.INTERMEDIATE)
                .points(0)
                .level(1)
                .reputationScore(BigDecimal.ZERO)
                .onboardingComplete(true)
                .active(true)
                .build();
    }
}
