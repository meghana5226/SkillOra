package com.skillora.service;

import com.skillora.dto.swap.SwapRequestCreateRequest;
import com.skillora.dto.swap.SwapRequestResponse;
import com.skillora.entity.*;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;

    @Transactional
    public SwapRequestResponse create(Long senderId, SwapRequestCreateRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new BadRequestException("You cannot send a swap request to yourself");
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        Skill offeredSkill = skillRepository.findById(request.getOfferedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Offered skill not found"));
        Skill requestedSkill = skillRepository.findById(request.getRequestedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested skill not found"));

        SwapRequest swap = SwapRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .offeredSkill(offeredSkill)
                .requestedSkill(requestedSkill)
                .message(request.getMessage())
                .status(SwapStatus.PENDING)
                .build();
        swap = swapRequestRepository.save(swap);

        notificationService.notify(receiver, "SWAP_REQUEST",
                "New swap request from " + sender.getName(),
                sender.getName() + " wants to teach you " + offeredSkill.getName() +
                        " in exchange for learning " + requestedSkill.getName() + ".");

        return toResponse(swap);
    }

    @Transactional(readOnly = true)
    public Page<SwapRequestResponse> sent(Long userId, Pageable pageable) {
        return swapRequestRepository.findBySenderIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SwapRequestResponse> received(Long userId, Pageable pageable) {
        return swapRequestRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public SwapRequestResponse accept(Long receiverId, Long swapId) {
        SwapRequest swap = swapRequestRepository.findByIdAndReceiverId(swapId, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));
        requireStatus(swap, SwapStatus.PENDING);
        swap.setStatus(SwapStatus.ACCEPTED);
        swapRequestRepository.save(swap);

        conversationService.getOrCreateConversation(swap.getSender().getId(), swap.getReceiver().getId());

        notificationService.notify(swap.getSender(), "SWAP_ACCEPTED",
                swap.getReceiver().getName() + " accepted your swap request",
                "You can now message each other and schedule a session for " + swap.getRequestedSkill().getName() + ".");

        return toResponse(swap);
    }

    @Transactional
    public SwapRequestResponse reject(Long receiverId, Long swapId) {
        SwapRequest swap = swapRequestRepository.findByIdAndReceiverId(swapId, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));
        requireStatus(swap, SwapStatus.PENDING);
        swap.setStatus(SwapStatus.REJECTED);
        swapRequestRepository.save(swap);

        notificationService.notify(swap.getSender(), "SWAP_REJECTED",
                "Your swap request was declined",
                swap.getReceiver().getName() + " declined your request for " + swap.getRequestedSkill().getName() + ".");

        return toResponse(swap);
    }

    @Transactional
    public SwapRequestResponse cancel(Long senderId, Long swapId) {
        SwapRequest swap = swapRequestRepository.findByIdAndSenderId(swapId, senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));
        if (swap.getStatus() != SwapStatus.PENDING && swap.getStatus() != SwapStatus.ACCEPTED) {
            throw new BadRequestException("This request can no longer be cancelled");
        }
        swap.setStatus(SwapStatus.CANCELLED);
        swapRequestRepository.save(swap);
        return toResponse(swap);
    }

    private void requireStatus(SwapRequest swap, SwapStatus expected) {
        if (swap.getStatus() != expected) {
            throw new BadRequestException("This request is no longer " + expected.name().toLowerCase());
        }
    }

    private SwapRequestResponse toResponse(SwapRequest s) {
        return SwapRequestResponse.builder()
                .id(s.getId())
                .senderId(s.getSender().getId())
                .senderName(s.getSender().getName())
                .receiverId(s.getReceiver().getId())
                .receiverName(s.getReceiver().getName())
                .offeredSkillId(s.getOfferedSkill().getId())
                .offeredSkillName(s.getOfferedSkill().getName())
                .requestedSkillId(s.getRequestedSkill().getId())
                .requestedSkillName(s.getRequestedSkill().getName())
                .message(s.getMessage())
                .status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
