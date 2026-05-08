package com.adnanumar.task_manager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse(
        Long id,
        String name,
        String description,
        UserSummary createdBy,
        List<MemberResponse> members,
        List<TaskResponse> tasks,
        LocalDateTime createdAt
) {}
