package com.adnanumar.task_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Name must be under 100 characters")
        String name,

        @Size(max = 500, message = "Description must be under 500 characters")
        String description
) {}
