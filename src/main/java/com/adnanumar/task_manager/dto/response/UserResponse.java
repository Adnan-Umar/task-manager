package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String globalRole,
        LocalDateTime createdAt
) {
    // Static factory method — convert entity → record
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGlobalRole().name(),
                user.getCreatedAt()
        );
    }
}
