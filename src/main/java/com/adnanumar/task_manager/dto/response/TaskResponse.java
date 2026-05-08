package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDate dueDate,
        boolean isOverdue,
        UserSummary assignedTo,   // nullable
        UserSummary createdBy,
        Long projectId,
        String projectName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getDueDate(),
                // isOverdue = dueDate passed AND task not DONE
                task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && task.getStatus() != TaskStatus.DONE,
                task.getAssignedTo() != null
                        ? UserSummary.fromEntity(task.getAssignedTo()) : null,
                UserSummary.fromEntity(task.getCreatedBy()),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
