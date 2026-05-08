package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.User;

public record UserSummary(
        Long id,
        String name,
        String email
) {
    // Static factory method — lightweight version for nested use inside Task/Project responses
    public static UserSummary fromEntity(User user) {
        return new UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
