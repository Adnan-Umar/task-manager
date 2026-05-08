package com.adnanumar.task_manager.controller;

import com.adnanumar.task_manager.dto.response.ApiResponse;
import com.adnanumar.task_manager.dto.response.DashboardResponse;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.enums.TaskStatus;
import com.adnanumar.task_manager.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    DashboardService dashboardService;

    // GET /api/dashboard
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/dashboard — user: {}", userDetails.getUsername());
        DashboardResponse response = dashboardService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched", response));
    }

    // GET /api/dashboard/my-tasks?status=TODO
    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TaskStatus status) {

        log.info("GET /api/dashboard/my-tasks — user: {}, statusFilter: {}", userDetails.getUsername(), status);
        List<TaskResponse> tasks = dashboardService.getMyTasks(userDetails.getUsername(), status);
        return ResponseEntity.ok(ApiResponse.success("My tasks fetched", tasks));
    }

    // GET /api/dashboard/overdue
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/dashboard/overdue — user: {}", userDetails.getUsername());
        List<TaskResponse> tasks = dashboardService.getOverdueTasks(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks fetched", tasks));
    }
}
