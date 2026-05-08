package com.adnanumar.task_manager.repository;

import com.adnanumar.task_manager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedToId(Long userId);

    // Overdue tasks (dueDate passed and not DONE)
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId " +
            "AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("userId") Long userId,
                                @Param("today") LocalDate today);

    @Query("SELECT t.status, COUNT(t) FROM Task t " +
            "WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countTasksByStatus(@Param("projectId") Long projectId);

    List<Task> findByDueDateAndStatusNot(java.time.LocalDate dueDate, com.adnanumar.task_manager.enums.TaskStatus status);
}
