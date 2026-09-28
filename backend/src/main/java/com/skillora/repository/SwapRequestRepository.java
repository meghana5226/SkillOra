package com.skillora.repository;

import com.skillora.entity.SwapRequest;
import com.skillora.entity.SwapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {
    Page<SwapRequest> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);
    Page<SwapRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    long countByStatus(SwapStatus status);
    Optional<SwapRequest> findByIdAndReceiverId(Long id, Long receiverId);
    Optional<SwapRequest> findByIdAndSenderId(Long id, Long senderId);
}
