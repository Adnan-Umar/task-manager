package com.adnanumar.task_manager.controller;

import com.adnanumar.task_manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/projects/{projectId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ByteArrayInputStream in = reportService.exportProjectTasksToExcel(projectId, userDetails.getUsername());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=project_" + projectId + "_tasks.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> exportPdf(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ByteArrayInputStream in = reportService.exportProjectTasksToPdf(projectId, userDetails.getUsername());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=project_" + projectId + "_tasks.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
