package com.adnanumar.task_manager.dto.request;

import com.adnanumar.task_manager.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Title must be under 200 characters")
        String title,

        @Size(max = 1000, message = "Description must be under 1000 characters")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority,

        @Future(message = "Due date must be in the future")
        LocalDate dueDate,

        Long assignedToId    // nullable — can be unassigned
) {}
