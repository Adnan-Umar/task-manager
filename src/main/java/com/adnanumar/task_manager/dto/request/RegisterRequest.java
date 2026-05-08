package com.adnanumar.task_manager.dto.request;

import com.adnanumar.task_manager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        Role globalRole   // optional — defaults to MEMBER in service
) {
    // Compact constructor to set default role
    public RegisterRequest {
        if (globalRole == null) globalRole = Role.MEMBER;
    }
}
