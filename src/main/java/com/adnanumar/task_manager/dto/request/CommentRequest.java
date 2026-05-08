package com.adnanumar.task_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 1000, message = "Comment must be under 1000 characters")
    String content
) {}
