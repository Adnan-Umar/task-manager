package com.adnanumar.task_manager.service;

public interface EmailService {
    void sendDeadlineAlert(String to, String userName, String taskTitle, String dueDate);
}
