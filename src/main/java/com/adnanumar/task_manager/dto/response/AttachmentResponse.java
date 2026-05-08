package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.Attachment;
import java.time.LocalDateTime;

public record AttachmentResponse(
    Long id,
    String fileName,
    String fileType,
    Long fileSize,
    LocalDateTime createdAt,
    UserSummary uploadedBy
) {
    public static AttachmentResponse fromEntity(Attachment attachment) {
        return new AttachmentResponse(
            attachment.getId(),
            attachment.getFileName(),
            attachment.getFileType(),
            attachment.getFileSize(),
            attachment.getCreatedAt(),
            UserSummary.fromEntity(attachment.getUploadedBy())
        );
    }
}
