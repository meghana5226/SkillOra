package com.skillora.service;

import com.skillora.dto.message.ConversationResponse;
import com.skillora.dto.message.MessageRequest;
import com.skillora.dto.message.MessageResponse;
import com.skillora.entity.Conversation;
import com.skillora.entity.Message;
import com.skillora.entity.User;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.repository.ConversationRepository;
import com.skillora.repository.MessageRepository;
import com.skillora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Conversation getOrCreateConversation(Long userAId, Long userBId) {
        return conversationRepository.findBetween(userAId, userBId).orElseGet(() -> {
            User a = userRepository.findById(userAId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            User b = userRepository.findById(userBId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return conversationRepository.save(Conversation.builder().participant1(a).participant2(b).build());
        });
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listForUser(Long userId) {
        return conversationRepository.findAllForUser(userId).stream().map(c -> {
            User other = c.getParticipant1().getId().equals(userId) ? c.getParticipant2() : c.getParticipant1();
            List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());
            Message last = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            long unread = messageRepository.countByConversationIdAndReadFalseAndSenderIdNot(c.getId(), userId);

            return ConversationResponse.builder()
                    .id(c.getId())
                    .otherUserId(other.getId())
                    .otherUserName(other.getName())
                    .otherUserProfileImage(other.getProfileImage())
                    .lastMessage(last == null ? null : last.getContent())
                    .lastMessageAt(last == null ? c.getCreatedAt() : last.getSentAt())
                    .unreadCount(unread)
                    .build();
        }).toList();
    }

    @Transactional
    public List<MessageResponse> getMessages(Long userId, Long conversationId) {
        Conversation conversation = requireParticipant(userId, conversationId);

        // mark incoming messages as read
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
        messages.stream()
                .filter(m -> !m.getSender().getId().equals(userId) && !m.getRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });

        return messages.stream().map(this::toResponse).toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, MessageRequest request) {
        Conversation conversation = requireParticipant(userId, conversationId);
        User sender = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .read(false)
                .build();
        message = messageRepository.save(message);

        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversationRepository.save(conversation);

        User other = conversation.getParticipant1().getId().equals(userId)
                ? conversation.getParticipant2() : conversation.getParticipant1();
        notificationService.notify(other, "NEW_MESSAGE",
                "New message from " + sender.getName(),
                sender.getName() + " sent you a message.");

        return toResponse(message);
    }

    private Conversation requireParticipant(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        boolean participant = conversation.getParticipant1().getId().equals(userId)
                || conversation.getParticipant2().getId().equals(userId);
        if (!participant) {
            throw new BadRequestException("You are not part of this conversation");
        }
        return conversation;
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .read(m.getRead())
                .build();
    }
}
