package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.request.CommentRequest;
import com.adnanumar.task_manager.dto.response.CommentResponse;
import com.adnanumar.task_manager.entity.Comment;
import com.adnanumar.task_manager.entity.Task;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.CommentRepository;
import com.adnanumar.task_manager.repository.ProjectMemberRepository;
import com.adnanumar.task_manager.repository.TaskRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long projectId, Long taskId, CommentRequest request, String email) {
        log.info("Adding comment to task {} in project {} by user {}", taskId, projectId, email);
        
        User user = getUserByEmailOrThrow(email);
        assertMembership(projectId, user.getId());
        
        Task task = getTaskOrThrow(taskId);
        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task does not belong to this project");
        }

        Comment comment = Comment.builder()
                .content(request.content())
                .task(task)
                .user(user)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(Long projectId, Long taskId, String email) {
        log.info("Fetching comments for task {} in project {} by user {}", taskId, projectId, email);
        
        User user = getUserByEmailOrThrow(email);
        assertMembership(projectId, user.getId());
        
        Task task = getTaskOrThrow(taskId);
        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task does not belong to this project");
        }

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long projectId, Long taskId, Long commentId, String email) {
        log.info("Deleting comment {} from task {} by user {}", commentId, taskId, email);
        
        User user = getUserByEmailOrThrow(email);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId.toString()));

        if (!comment.getTask().getId().equals(taskId) || !comment.getTask().getProject().getId().equals(projectId)) {
            throw new BadRequestException("Comment does not belong to this task/project");
        }

        // Only the author can delete their comment
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId.toString()));
    }

    private void assertMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new BadRequestException("You are not a member of this project");
        }
    }
}
