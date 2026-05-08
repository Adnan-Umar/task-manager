package com.adnanumar.task_manager.dto.request;

import com.adnanumar.task_manager.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Role is required")
        Role role    // ADMIN or MEMBER
) {}
