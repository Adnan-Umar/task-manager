package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.response.DashboardResponse;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.enums.TaskStatus;

import java.util.List;

public interface DashboardService {

    DashboardResponse getDashboard(String email);

    List<TaskResponse> getMyTasks(String email, TaskStatus statusFilter);

    List<TaskResponse> getOverdueTasks(String email);
}
