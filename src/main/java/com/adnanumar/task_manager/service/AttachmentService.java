package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.response.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    AttachmentResponse uploadFile(Long projectId, Long taskId, MultipartFile file, String email);
    List<AttachmentResponse> getAttachmentsByTask(Long projectId, Long taskId, String email);
    void deleteAttachment(Long projectId, Long taskId, Long attachmentId, String email);
    byte[] downloadFile(Long projectId, Long taskId, Long attachmentId, String email);
}
