package com.adnanumar.task_manager.dto.request;

import com.adnanumar.task_manager.enums.Priority;
import com.adnanumar.task_manager.enums.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(max = 200)
        String title,

        @Size(max = 1000)
        String description,

        Priority priority,

        TaskStatus status,

        LocalDate dueDate,

        Long assignedToId
) {}
