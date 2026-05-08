package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendDeadlineAlert(String to, String userName, String taskTitle, String dueDate) {
        log.info("Sending deadline alert from {} to: {}", fromEmail, to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Task Deadline Alert: " + taskTitle);
            message.setText(String.format(
                "Hello %s,\n\nThis is a reminder that the task '%s' is due on %s.\n\nPlease ensure it's completed on time.\n\nBest regards,\nEthara AI Task Manager",
                userName, taskTitle, dueDate
            ));
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
