package com.skillora.dto.session;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SessionCreateRequest {

    @NotNull(message = "receiverId (the person teaching) is required")
    private Long receiverId;

    @NotNull(message = "skillId is required")
    private Long skillId;

    @NotNull(message = "scheduledDate is required")
    private LocalDate scheduledDate;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    private String meetingLink;

    private String notes;
}
