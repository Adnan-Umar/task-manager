package com.adnanumar.task_manager.service;

import java.io.ByteArrayInputStream;

public interface ReportService {
    ByteArrayInputStream exportProjectTasksToExcel(Long projectId, String email);
    ByteArrayInputStream exportProjectTasksToPdf(Long projectId, String email);
}
