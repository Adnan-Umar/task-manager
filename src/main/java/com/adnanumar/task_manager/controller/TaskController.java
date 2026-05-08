package com.adnanumar.task_manager.controller;

import com.adnanumar.task_manager.dto.request.TaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskStatusRequest;
import com.adnanumar.task_manager.dto.response.ApiResponse;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.enums.Priority;
import com.adnanumar.task_manager.enums.TaskStatus;
import com.adnanumar.task_manager.service.TaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

    TaskService taskService;

    // POST /api/projects/{projectId}/tasks
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("POST /api/projects/{}/tasks — user: {}, title: {}", projectId, userDetails.getUsername(), request.title());
        TaskResponse response = taskService.createTask(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    // GET /api/projects/{projectId}/tasks?status=TODO&priority=HIGH
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority) {

        log.info("GET /api/projects/{}/tasks — user: {}, status: {}, priority: {}",
                projectId, userDetails.getUsername(), status, priority);
        List<TaskResponse> tasks = taskService.getTasksByProject(
                projectId, userDetails.getUsername(), status, priority);
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched", tasks));
    }

    // GET /api/projects/{projectId}/tasks/{taskId}
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/projects/{}/tasks/{} — user: {}", projectId, taskId, userDetails.getUsername());
        TaskResponse response = taskService.getTaskById(projectId, taskId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task fetched", response));
    }

    // PUT /api/projects/{projectId}/tasks/{taskId}
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PUT /api/projects/{}/tasks/{} — user: {}", projectId, taskId, userDetails.getUsername());
        TaskResponse response = taskService.updateTask(projectId, taskId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    // PATCH /api/projects/{projectId}/tasks/{taskId}/status
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PATCH /api/projects/{}/tasks/{}/status — user: {}, newStatus: {}",
                projectId, taskId, userDetails.getUsername(), request.status());
        TaskResponse response = taskService.updateTaskStatus(
                projectId, taskId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task status updated", response));
    }

    // DELETE /api/projects/{projectId}/tasks/{taskId}
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("DELETE /api/projects/{}/tasks/{} — user: {}", projectId, taskId, userDetails.getUsername());
        taskService.deleteTask(projectId, taskId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }
}
