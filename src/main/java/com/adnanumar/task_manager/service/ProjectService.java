package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.request.AddMemberRequest;
import com.adnanumar.task_manager.dto.request.ProjectRequest;
import com.adnanumar.task_manager.dto.response.MemberResponse;
import com.adnanumar.task_manager.dto.response.ProjectDetailResponse;
import com.adnanumar.task_manager.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    // ─── Project CRUD ────────────────────────────────────────────────────────────

    ProjectResponse createProject(ProjectRequest request, String creatorEmail);

    List<ProjectResponse> getAllProjects(String email);

    ProjectDetailResponse getProjectById(Long projectId, String email);

    ProjectResponse updateProject(Long projectId, ProjectRequest request, String email);

    void deleteProject(Long projectId, String email);

    // ─── Member Management ───────────────────────────────────────────────────────

    MemberResponse addMember(Long projectId, AddMemberRequest request, String requestingEmail);

    List<MemberResponse> getMembers(Long projectId, String requestingEmail);

    void removeMember(Long projectId, Long targetUserId, String requestingEmail);
}
