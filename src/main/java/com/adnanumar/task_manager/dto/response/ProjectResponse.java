package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserSummary createdBy,
        int totalMembers,
        int totalTasks,
        LocalDateTime createdAt
) {
    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserSummary.fromEntity(project.getCreatedBy()),
                project.getMembers().size(),
                project.getTasks().size(),
                project.getCreatedAt()
        );
    }
}
