package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    UserSummary user,
    LocalDateTime createdAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            UserSummary.fromEntity(comment.getUser()),
            comment.getCreatedAt()
        );
    }
}
