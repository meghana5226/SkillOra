package com.skillora.repository;

import com.skillora.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);
    long countByConversationIdAndReadFalseAndSenderIdNot(Long conversationId, Long senderId);
}
