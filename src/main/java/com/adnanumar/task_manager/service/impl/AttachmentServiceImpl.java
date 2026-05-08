package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.response.AttachmentResponse;
import com.adnanumar.task_manager.entity.Attachment;
import com.adnanumar.task_manager.entity.Project;
import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.AttachmentRepository;
import com.adnanumar.task_manager.repository.ProjectMemberRepository;
import com.adnanumar.task_manager.repository.ProjectRepository;
import com.adnanumar.task_manager.repository.TaskRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private final String uploadDir = "uploads/";

    @Override
    @Transactional
    public AttachmentResponse uploadFile(Long projectId, Long taskId, MultipartFile file, String email) {
        log.info("User '{}' uploading file to task {} in project {}", email, taskId, projectId);
        
        User user = getUserOrThrow(email);
        Task task = getTaskOrThrow(taskId, projectId);
        assertMembership(projectId, user.getId());

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, file.getBytes());

            Attachment attachment = Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .filePath(path.toString())
                    .fileSize(file.getSize())
                    .task(task)
                    .uploadedBy(user)
                    .build();

            Attachment saved = attachmentRepository.save(attachment);
            return AttachmentResponse.fromEntity(saved);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTask(Long projectId, Long taskId, String email) {
        User user = getUserOrThrow(email);
        getTaskOrThrow(taskId, projectId);
        assertMembership(projectId, user.getId());

        return attachmentRepository.findByTaskId(taskId).stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttachment(Long projectId, Long taskId, Long attachmentId, String email) {
        User user = getUserOrThrow(email);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId.toString()));
        
        if (!attachment.getTask().getId().equals(taskId)) {
            throw new BadRequestException("Attachment does not belong to this task");
        }

        assertMembership(projectId, user.getId());

        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            attachmentRepository.delete(attachment);
        } catch (IOException e) {
            log.error("Failed to delete file", e);
            attachmentRepository.delete(attachment); // Delete from DB anyway
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long projectId, Long taskId, Long attachmentId, String email) {
        User user = getUserOrThrow(email);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId.toString()));
        
        assertMembership(projectId, user.getId());

        try {
            return Files.readAllBytes(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            log.error("Failed to read file", e);
            throw new BadRequestException("Could not read file: " + e.getMessage());
        }
    }

    // Helpers (reused from TaskServiceImpl logic)
    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private Task getTaskOrThrow(Long taskId, Long projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId.toString()));
        if (!task.getProject().getId().equals(projectId))
            throw new BadRequestException("Task does not belong to the specified project");
        return task;
    }

    private void assertMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new BadRequestException("You are not a member of this project");
        }
    }
}
