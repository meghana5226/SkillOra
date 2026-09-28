package com.skillora.repository;

import com.skillora.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByActiveTrueAndIdNot(Long id, Pageable pageable);
    Page<User> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    long countByActiveTrue();
    long countByCreatedAtAfter(java.time.LocalDateTime since);
}
