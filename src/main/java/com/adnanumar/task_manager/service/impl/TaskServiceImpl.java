package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.request.TaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskRequest;
import com.adnanumar.task_manager.dto.request.UpdateTaskStatusRequest;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.entity.Project;
import com.adnanumar.task_manager.entity.ProjectMember;
import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.enums.Priority;
import com.adnanumar.task_manager.enums.Role;
import com.adnanumar.task_manager.enums.TaskStatus;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.ProjectMemberRepository;
import com.adnanumar.task_manager.repository.ProjectRepository;
import com.adnanumar.task_manager.repository.TaskRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.TaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {

    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request, String creatorEmail) {
        log.info("User '{}' creating task '{}' in projectId {}", creatorEmail, request.title(), projectId);
        User creator = getUserOrThrow(creatorEmail);
        Project project = getProjectOrThrow(projectId);
        assertMembership(projectId, creator.getId());

        User assignedTo = null;
        if (request.assignedToId() != null) {
            assertMembership(projectId, request.assignedToId());
            assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.assignedToId().toString()));
            log.debug("Task assigned to userId: {}", request.assignedToId());
        }

        Task saved = taskRepository.save(Task.builder()
                .title(request.title()).description(request.description())
                .priority(request.priority()).dueDate(request.dueDate())
                .project(project).createdBy(creator).assignedTo(assignedTo)
                .status(TaskStatus.TODO)
                .build());
        log.info("Task created — id: {}, projectId: {}", saved.getId(), projectId);
        return TaskResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId, String email,
                                                TaskStatus statusFilter, Priority priorityFilter) {
        log.info("User '{}' fetching tasks for projectId {} [status={}, priority={}]",
                email, projectId, statusFilter, priorityFilter);
        User user = getUserOrThrow(email);
        getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());

        Stream<Task> stream = taskRepository.findByProjectId(projectId).stream();
        if (statusFilter != null)   stream = stream.filter(t -> t.getStatus() == statusFilter);
        if (priorityFilter != null) stream = stream.filter(t -> t.getPriority() == priorityFilter);

        List<TaskResponse> result = stream.map(TaskResponse::fromEntity).toList();
        log.debug("Returning {} tasks for projectId {}", result.size(), projectId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long projectId, Long taskId, String email) {
        log.info("User '{}' fetching taskId {} in projectId {}", email, taskId, projectId);
        User user = getUserOrThrow(email);
        getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());
        return TaskResponse.fromEntity(getTaskOrThrow(taskId, projectId));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, UpdateTaskRequest request, String email) {
        log.info("User '{}' updating taskId {} in projectId {}", email, taskId, projectId);
        User user = getUserOrThrow(email);
        getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());
        Task task = getTaskOrThrow(taskId, projectId);

        if (request.title() != null)       task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null)    task.setPriority(request.priority());
        if (request.status() != null)      task.setStatus(request.status());
        if (request.dueDate() != null)     task.setDueDate(request.dueDate());
        if (request.assignedToId() != null) {
            assertMembership(projectId, request.assignedToId());
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.assignedToId().toString()));
            task.setAssignedTo(assignee);
            log.debug("Task {} reassigned to userId {}", taskId, request.assignedToId());
        }

        log.info("Task {} updated in projectId {}", taskId, projectId);
        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long projectId, Long taskId,
                                         UpdateTaskStatusRequest request, String email) {
        log.info("User '{}' patching status of taskId {} to {}", email, taskId, request.status());
        User user = getUserOrThrow(email);
        getProjectOrThrow(projectId);
        assertMembership(projectId, user.getId());
        Task task = getTaskOrThrow(taskId, projectId);
        task.setStatus(request.status());
        log.info("Task {} status → {} in projectId {}", taskId, request.status(), projectId);
        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long taskId, String email) {
        log.info("User '{}' deleting taskId {} in projectId {}", email, taskId, projectId);
        User user = getUserOrThrow(email);
        getProjectOrThrow(projectId);
        assertAdminRole(projectId, user.getId());
        taskRepository.delete(getTaskOrThrow(taskId, projectId));
        log.info("Task {} deleted from projectId {} by '{}'", taskId, projectId, email);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found: {}", id);
                    return new ResourceNotFoundException("Project", id.toString());
                });
    }

    private Task getTaskOrThrow(Long taskId, Long projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task {} not found", taskId);
                    return new ResourceNotFoundException("Task", taskId.toString());
                });
        if (!task.getProject().getId().equals(projectId))
            throw new BadRequestException("Task does not belong to the specified project");
        return task;
    }

    private void assertMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByUserIdAndProjectId(userId, projectId)) {
            log.warn("Access denied — userId {} not a member of projectId {}", userId, projectId);
            throw new BadRequestException("You are not a member of this project");
        }
    }

    private void assertAdminRole(Long projectId, Long userId) {
        ProjectMember m = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this project"));
        if (m.getRole() != Role.ADMIN)
            throw new BadRequestException("Only project admins can delete tasks");
    }
}
