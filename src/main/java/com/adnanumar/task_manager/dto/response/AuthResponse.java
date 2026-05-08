package com.adnanumar.task_manager.dto.response;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role
) {}
