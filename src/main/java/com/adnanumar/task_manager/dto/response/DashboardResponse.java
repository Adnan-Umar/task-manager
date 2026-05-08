package com.adnanumar.task_manager.dto.response;

import java.util.List;

public record DashboardResponse(
        int totalProjects,
        int totalTasks,
        int todoCount,
        int inProgressCount,
        int doneCount,
        int overdueCount,
        List<TaskResponse> recentTasks,
        List<ProjectSummary> projects
) {}
