package com.skillora.service;

import com.skillora.dto.review.ReviewCreateRequest;
import com.skillora.dto.review.ReviewResponse;
import com.skillora.entity.Review;
import com.skillora.entity.Session;
import com.skillora.entity.SessionStatus;
import com.skillora.entity.User;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ConflictException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.repository.ReviewRepository;
import com.skillora.repository.SessionRepository;
import com.skillora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponse create(Long reviewerId, ReviewCreateRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new BadRequestException("You can only review completed sessions");
        }

        boolean isRequester = session.getRequester().getId().equals(reviewerId);
        boolean isReceiver = session.getReceiver().getId().equals(reviewerId);
        if (!isRequester && !isReceiver) {
            throw new BadRequestException("You were not part of this session");
        }

        if (reviewRepository.existsBySessionIdAndReviewerId(session.getId(), reviewerId)) {
            throw new ConflictException("You already reviewed this session");
        }

        User reviewer = userRepository.getReferenceById(reviewerId);
        User reviewedUser = isRequester ? session.getReceiver() : session.getRequester();

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewedUser(reviewedUser)
                .session(session)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);

        updateReputation(reviewedUser.getId());

        if (request.getRating() == 5) {
            User fullReviewedUser = userRepository.findById(reviewedUser.getId()).orElseThrow();
            gamificationService.awardPoints(fullReviewedUser, GamificationService.XP_FIVE_STAR_REVIEW,
                    "Received a 5-star review");
        }

        User fullReviewedUser = userRepository.findById(reviewedUser.getId()).orElseThrow();
        notificationService.notify(fullReviewedUser, "REVIEW_RECEIVED",
                "You received a " + request.getRating() + "-star review",
                reviewer.getName() + " left you a review.");

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> forUser(Long userId, Pageable pageable) {
        return reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    private void updateReputation(Long userId) {
        Double avg = reviewRepository.averageRatingForUser(userId);
        if (avg == null) return;
        User user = userRepository.findById(userId).orElseThrow();
        user.setReputationScore(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getName())
                .reviewedUserId(r.getReviewedUser().getId())
                .sessionId(r.getSession().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
