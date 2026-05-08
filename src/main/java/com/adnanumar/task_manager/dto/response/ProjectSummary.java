package com.adnanumar.task_manager.dto.response;

// Lightweight project summary for use inside DashboardResponse
public record ProjectSummary(
        Long id,
        String name,
        int todoCount,
        int inProgressCount,
        int doneCount
) {}
