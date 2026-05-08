package com.adnanumar.task_manager.scheduler;

import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.enums.TaskStatus;
import com.adnanumar.task_manager.repository.TaskRepository;
import com.adnanumar.task_manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineScheduler {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    /**
     * Runs every day at 8:00 AM to send alerts for tasks due tomorrow.
     * Cron expression: "0 0 8 * * ?"
     * For testing, you can use "0 * * * * ?" (every minute)
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendUpcomingDeadlineAlerts() {
        log.info("Starting scheduled task: Upcoming Deadline Alerts (Tomorrow)");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> upcomingTasks = taskRepository.findByDueDateAndStatusNot(tomorrow, TaskStatus.DONE);
        
        log.info("Found {} tasks due tomorrow ({})", upcomingTasks.size(), tomorrow);
        
        for (Task task : upcomingTasks) {
            if (task.getAssignedTo() != null) {
                emailService.sendDeadlineAlert(
                    task.getAssignedTo().getEmail(),
                    task.getAssignedTo().getName(),
                    task.getTitle(),
                    task.getDueDate().toString()
                );
            }
        }
        
        log.info("Finished scheduled task: Upcoming Deadline Alerts (Tomorrow)");
    }

    /**
     * Runs every day at 9:00 AM to send alerts for tasks due TODAY.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendTodayDeadlineAlerts() {
        log.info("Starting scheduled task: Today's Deadline Alerts");

        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskRepository.findByDueDateAndStatusNot(today, TaskStatus.DONE);

        log.info("Found {} tasks due today ({})", todayTasks.size(), today);

        for (Task task : todayTasks) {
            if (task.getAssignedTo() != null) {
                emailService.sendDeadlineAlert(
                        task.getAssignedTo().getEmail(),
                        task.getAssignedTo().getName(),
                        task.getTitle(),
                        "TODAY (" + task.getDueDate().toString() + ")"
                );
            }
        }

        log.info("Finished scheduled task: Today's Deadline Alerts");
    }
}
