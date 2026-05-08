package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.response.DashboardResponse;
import com.adnanumar.task_manager.dto.response.ProjectSummary;
import com.adnanumar.task_manager.dto.response.TaskResponse;
import com.adnanumar.task_manager.entity.Project;
import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.enums.TaskStatus;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.ProjectRepository;
import com.adnanumar.task_manager.repository.TaskRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {

    ProjectRepository projectRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email) {
        log.info("Building dashboard for user: '{}'", email);
        User user = getUserOrThrow(email);

        List<Project> projects = projectRepository.findProjectsByUserId(user.getId());
        List<Task> allTasks = projects.stream().flatMap(p -> p.getTasks().stream()).toList();

        int totalProjects   = projects.size();
        int totalTasks      = allTasks.size();
        int todoCount       = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        int inProgressCount = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int doneCount       = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int overdueCount    = (int) allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(LocalDate.now())
                        && t.getStatus() != TaskStatus.DONE)
                .count();

        List<TaskResponse> recentTasks = allTasks.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(TaskResponse::fromEntity)
                .toList();

        List<ProjectSummary> projectSummaries = projects.stream()
                .map(p -> buildProjectSummary(p.getId(), p.getName()))
                .toList();

        log.debug("Dashboard for '{}': {} projects, {} tasks, {} overdue", email, totalProjects, totalTasks, overdueCount);

        return new DashboardResponse(totalProjects, totalTasks, todoCount,
                inProgressCount, doneCount, overdueCount, recentTasks, projectSummaries);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(String email, TaskStatus statusFilter) {
        log.info("Fetching tasks assigned to '{}' [statusFilter={}]", email, statusFilter);
        User user = getUserOrThrow(email);

        List<Task> tasks = taskRepository.findByAssignedToId(user.getId());

        if (statusFilter != null) {
            tasks = tasks.stream().filter(t -> t.getStatus() == statusFilter).toList();
        }

        log.debug("Found {} tasks for '{}'", tasks.size(), email);
        return tasks.stream().map(TaskResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String email) {
        log.info("Fetching overdue tasks for user: '{}'", email);
        User user = getUserOrThrow(email);
        List<Task> tasks = taskRepository.findOverdueTasks(user.getId(), LocalDate.now());
        log.debug("Found {} overdue tasks for '{}'", tasks.size(), email);
        return tasks.stream().map(TaskResponse::fromEntity).toList();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private ProjectSummary buildProjectSummary(Long projectId, String projectName) {
        List<Object[]> rows = taskRepository.countTasksByStatus(projectId);
        int todo = 0, inProgress = 0, done = 0;
        for (Object[] row : rows) {
            TaskStatus status = (TaskStatus) row[0];
            long count = (long) row[1];
            switch (status) {
                case TODO        -> todo = (int) count;
                case IN_PROGRESS -> inProgress = (int) count;
                case DONE        -> done = (int) count;
            }
        }
        return new ProjectSummary(projectId, projectName, todo, inProgress, done);
    }
}
