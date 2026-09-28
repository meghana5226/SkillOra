package com.skillora.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long receiverId;
    private String receiverName;
    private Long skillId;
    private String skillName;
    private LocalDate scheduledDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String meetingLink;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private Boolean hasReview;
}
