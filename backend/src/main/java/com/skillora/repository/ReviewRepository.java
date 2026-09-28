package com.skillora.repository;

import com.skillora.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByReviewedUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsBySessionIdAndReviewerId(Long sessionId, Long reviewerId);
    List<Review> findBySessionId(Long sessionId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewedUser.id = :userId")
    Double averageRatingForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUser.id = :userId")
    long countForUser(@Param("userId") Long userId);
}
