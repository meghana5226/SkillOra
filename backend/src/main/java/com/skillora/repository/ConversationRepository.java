package com.skillora.repository;

import com.skillora.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.participant1.id = :userId OR c.participant2.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findAllForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1.id = :u1 AND c.participant2.id = :u2) OR " +
           "(c.participant1.id = :u2 AND c.participant2.id = :u1)")
    Optional<Conversation> findBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
