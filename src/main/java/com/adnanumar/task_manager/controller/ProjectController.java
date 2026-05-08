package com.adnanumar.task_manager.controller;

import com.adnanumar.task_manager.dto.request.AddMemberRequest;
import com.adnanumar.task_manager.dto.request.ProjectRequest;
import com.adnanumar.task_manager.dto.response.ApiResponse;
import com.adnanumar.task_manager.dto.response.MemberResponse;
import com.adnanumar.task_manager.dto.response.ProjectDetailResponse;
import com.adnanumar.task_manager.dto.response.ProjectResponse;
import com.adnanumar.task_manager.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

    ProjectService projectService;

    // ─── Project Endpoints ───────────────────────────────────────────────────────

    // POST /api/projects
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("POST /api/projects — user: {}, name: {}", userDetails.getUsername(), request.name());
        ProjectResponse response = projectService.createProject(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    // GET /api/projects
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/projects — user: {}", userDetails.getUsername());
        List<ProjectResponse> projects = projectService.getAllProjects(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Projects fetched", projects));
    }

    // GET /api/projects/{projectId}
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/projects/{} — user: {}", projectId, userDetails.getUsername());
        ProjectDetailResponse response = projectService.getProjectById(projectId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project fetched", response));
    }

    // PUT /api/projects/{projectId}
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PUT /api/projects/{} — user: {}", projectId, userDetails.getUsername());
        ProjectResponse response = projectService.updateProject(projectId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    // DELETE /api/projects/{projectId}
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("DELETE /api/projects/{} — user: {}", projectId, userDetails.getUsername());
        projectService.deleteProject(projectId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    // ─── Member Endpoints ────────────────────────────────────────────────────────

    // POST /api/projects/{projectId}/members
    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("POST /api/projects/{}/members — user: {}, target: {}", projectId, userDetails.getUsername(), request.userId());
        MemberResponse response = projectService.addMember(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    // GET /api/projects/{projectId}/members
    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/projects/{}/members — user: {}", projectId, userDetails.getUsername());
        List<MemberResponse> members = projectService.getMembers(projectId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Members fetched", members));
    }

    // DELETE /api/projects/{projectId}/members/{userId}
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("DELETE /api/projects/{}/members/{} — user: {}", projectId, userId, userDetails.getUsername());
        projectService.removeMember(projectId, userId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }
}
