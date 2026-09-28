package com.skillora.dto.swap;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SwapRequestCreateRequest {

    @NotNull(message = "receiverId is required")
    private Long receiverId;

    @NotNull(message = "offeredSkillId is required")
    private Long offeredSkillId;

    @NotNull(message = "requestedSkillId is required")
    private Long requestedSkillId;

    @Size(max = 1000)
    private String message;
}
