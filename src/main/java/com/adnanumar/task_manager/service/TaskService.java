package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.request.TaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskStatusRequest;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.enums.Priority;
import com.adnanumar.task_manager.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long projectId, TaskRequest request, String creatorEmail);

    List<TaskResponse> getTasksByProject(Long projectId, String email,
                                         TaskStatus statusFilter, Priority priorityFilter);

    TaskResponse getTaskById(Long projectId, Long taskId, String email);

    TaskResponse updateTask(Long projectId, Long taskId, UpdateTaskRequest request, String email);

    TaskResponse updateTaskStatus(Long projectId, Long taskId, UpdateTaskStatusRequest request, String email);

    void deleteTask(Long projectId, Long taskId, String email);
}
