package com.skillora.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusRequest {

    @NotNull(message = "active is required")
    private Boolean active;
}
