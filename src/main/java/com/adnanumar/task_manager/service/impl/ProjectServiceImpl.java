package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.request.AddMemberRequest;
import com.adnanumar.task_manager.dto.request.ProjectRequest;
import com.adnanumar.task_manager.dto.response.MemberResponse;
import com.adnanumar.task_manager.dto.response.ProjectDetailResponse;
import com.adnanumar.task_manager.dto.response.ProjectResponse;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.dto.response.UserSummary;
import com.adnanumar.task_manager.entity.Project;
import com.adnanumar.task_manager.entity.ProjectMember;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.enums.Role;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.ProjectMemberRepository;
import com.adnanumar.task_manager.repository.ProjectRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;

    // ─── Project CRUD ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String creatorEmail) {
        log.info("Starting createProject for user: '{}', project: '{}'", creatorEmail, request.name());

        User creator = getUserByEmailOrThrow(creatorEmail);
        log.debug("Found creator: {} (ID: {})", creator.getEmail(), creator.getId());

        // 1. Initialize Project
        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(creator)
                .build();

        // 2. Initialize the creator as the first ADMIN member
        ProjectMember adminMember = ProjectMember.builder()
                .user(creator)
                .project(project)
                .role(Role.ADMIN)
                .build();

        // 3. Link them (bidirectional)
        if (project.getMembers() == null) {
            project.setMembers(new java.util.ArrayList<>());
        }
        project.getMembers().add(adminMember);

        // 4. Save project (cascades to members due to CascadeType.ALL)
        log.debug("Saving project entity and flushing...");
        Project saved = projectRepository.saveAndFlush(project);

        log.info("Project successfully saved with ID: {}. Member count: {}", 
                saved.getId(), saved.getMembers().size());

        return ProjectResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(String email) {
        log.info("Fetching projects for user: '{}'", email);
        User user = getUserByEmailOrThrow(email);
        List<Project> projects = projectRepository.findProjectsByUserId(user.getId());
        log.debug("Found {} projects for '{}'", projects.size(), email);
        return projects.stream().map(ProjectResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(Long projectId, String email) {
        log.info("User '{}' fetching detail for projectId: {}", email, projectId);

        User user = getUserByEmailOrThrow(email);
        Project project = getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());

        List<MemberResponse> members = project.getMembers().stream()
                .map(MemberResponse::fromEntity)
                .toList();

        List<TaskResponse> tasks = project.getTasks().stream()
                .map(TaskResponse::fromEntity)
                .toList();

        log.debug("Project {} detail: {} members, {} tasks", projectId, members.size(), tasks.size());

        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserSummary.fromEntity(project.getCreatedBy()),
                members,
                tasks,
                project.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, String email) {
        log.info("User '{}' updating projectId: {}", email, projectId);

        User user = getUserByEmailOrThrow(email);
        Project project = getProjectOrThrow(projectId);
        assertAdminRole(projectId, user.getId());

        project.setName(request.name());
        project.setDescription(request.description());

        Project updated = projectRepository.save(project);
        log.info("Project {} updated successfully", projectId);
        return ProjectResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String email) {
        log.info("User '{}' requesting deletion of projectId: {}", email, projectId);

        User user = getUserByEmailOrThrow(email);
        getProjectOrThrow(projectId);
        assertAdminRole(projectId, user.getId());

        projectRepository.deleteById(projectId);
        log.info("Project {} deleted by '{}'", projectId, email);
    }

    // ─── Member Management ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public MemberResponse addMember(Long projectId, AddMemberRequest request, String requestingEmail) {
        log.info("User '{}' adding userId {} to projectId {} with role {}",
                requestingEmail, request.userId(), projectId, request.role());

        User requestingUser = getUserByEmailOrThrow(requestingEmail);
        Project project = getProjectOrThrow(projectId);
        assertAdminRole(projectId, requestingUser.getId());

        if (projectMemberRepository.existsByUserIdAndProjectId(request.userId(), projectId)) {
            log.warn("UserId {} is already a member of projectId {}", request.userId(), projectId);
            throw new BadRequestException("User is already a member of this project");
        }

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("Target user not found: {}", request.userId());
                    return new ResourceNotFoundException("User", request.userId().toString());
                });

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(request.role())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);
        log.info("UserId {} added to project {} as {}", request.userId(), projectId, request.role());
        return MemberResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long projectId, String requestingEmail) {
        log.info("User '{}' fetching members of projectId {}", requestingEmail, projectId);

        User user = getUserByEmailOrThrow(requestingEmail);
        getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());

        List<MemberResponse> members = projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();

        log.debug("Found {} members in projectId {}", members.size(), projectId);
        return members;
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long targetUserId, String requestingEmail) {
        log.info("User '{}' removing userId {} from projectId {}", requestingEmail, targetUserId, projectId);

        User requestingUser = getUserByEmailOrThrow(requestingEmail);
        getProjectOrThrow(projectId);
        assertAdminRole(projectId, requestingUser.getId());

        if (targetUserId.equals(requestingUser.getId())) {
            log.warn("User '{}' tried to remove themselves from projectId {}", requestingEmail, projectId);
            throw new BadRequestException("Project admin cannot remove themselves. Transfer admin role first.");
        }

        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId)
                .orElseThrow(() -> {
                    log.warn("UserId {} is not a member of projectId {}", targetUserId, projectId);
                    return new BadRequestException("User is not a member of this project");
                });

        projectMemberRepository.delete(member);
        log.info("UserId {} removed from projectId {} by '{}'", targetUserId, projectId, requestingEmail);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: '{}'", email);
                    return new ResourceNotFoundException("User", email);
                });
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found: {}", projectId);
                    return new ResourceNotFoundException("Project", projectId.toString());
                });
    }

    private void assertMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByUserIdAndProjectId(userId, projectId)) {
            log.warn("Access denied — userId {} is not a member of projectId {}", userId, projectId);
            throw new BadRequestException("You are not a member of this project");
        }
    }

    private void assertAdminRole(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> {
                    log.warn("Access denied — userId {} is not a member of projectId {}", userId, projectId);
                    return new BadRequestException("You are not a member of this project");
                });

        if (member.getRole() != Role.ADMIN) {
            log.warn("Access denied — userId {} is not ADMIN in projectId {}", userId, projectId);
            throw new BadRequestException("Only project admins can perform this action");
        }
    }
}
