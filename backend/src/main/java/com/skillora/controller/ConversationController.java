package com.skillora.controller;

import com.skillora.dto.message.ConversationResponse;
import com.skillora.dto.message.MessageRequest;
import com.skillora.dto.message.MessageResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.ConversationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Messaging")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.listForUser(principal.getId()));
    }

    @PostMapping("/with/{userId}")
    public ResponseEntity<Long> startWith(@PathVariable Long userId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.getOrCreateConversation(principal.getId(), userId).getId());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.getMessages(principal.getId(), id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> send(@PathVariable Long id, @Valid @RequestBody MessageRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.sendMessage(principal.getId(), id, request));
    }
}
