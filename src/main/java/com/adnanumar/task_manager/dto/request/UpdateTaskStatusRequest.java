package com.adnanumar.task_manager.dto.request;

import com.adnanumar.task_manager.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Status is required")
        TaskStatus status
) {}
