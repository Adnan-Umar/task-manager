package com.adnanumar.task_manager.dto.response;

import com.adnanumar.task_manager.entity.ProjectMember;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        UserSummary user,
        String role,
        LocalDateTime joinedAt
) {
    public static MemberResponse fromEntity(ProjectMember member) {
        return new MemberResponse(
                member.getId(),
                UserSummary.fromEntity(member.getUser()),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
}
