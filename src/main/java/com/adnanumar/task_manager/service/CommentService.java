package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.request.CommentRequest;
import com.adnanumar.task_manager.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Long projectId, Long taskId, CommentRequest request, String email);
    List<CommentResponse> getCommentsByTask(Long projectId, Long taskId, String email);
    void deleteComment(Long projectId, Long taskId, Long commentId, String email);
}
