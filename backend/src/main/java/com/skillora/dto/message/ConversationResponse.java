package com.skillora.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
}
