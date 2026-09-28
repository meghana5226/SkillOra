package com.skillora.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    private String name;

    @Size(max = 1000, message = "Bio must be under 1000 characters")
    private String bio;

    @Size(max = 150)
    private String location;

    @Size(max = 80)
    private String timezone;

    private String experienceLevel;

    @Size(max = 500)
    private String availability;

    private String profileImage;
}
