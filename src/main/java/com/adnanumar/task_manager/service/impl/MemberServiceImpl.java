package com.adnanumar.task_manager.service.impl;

import com.adnanumar.task_manager.dto.request.AddMemberRequest;
import com.adnanumar.task_manager.dto.response.MemberResponse;
import com.adnanumar.task_manager.entity.Project;
import com.adnanumar.task_manager.entity.ProjectMember;
import com.adnanumar.task_manager.entity.User;
import com.adnanumar.task_manager.enums.Role;
import com.adnanumar.task_manager.error.BadRequestException;
import com.adnanumar.task_manager.error.ResourceNotFoundException;
import com.adnanumar.task_manager.repository.ProjectMemberRepository;
import com.adnanumar.task_manager.repository.ProjectRepository;
import com.adnanumar.task_manager.repository.UserRepository;
import com.adnanumar.task_manager.service.MemberService;
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
public class MemberServiceImpl implements MemberService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public MemberResponse addMember(Long projectId, AddMemberRequest request, Long requestingUserId) {
        log.info("User {} adding userId {} to projectId {} with role {}", requestingUserId, request.userId(), projectId, request.role());

        Project project = getProjectOrThrow(projectId);
        assertAdminRole(projectId, requestingUserId);

        if (projectMemberRepository.existsByUserIdAndProjectId(request.userId(), projectId)) {
            log.warn("User {} is already a member of projectId {}", request.userId(), projectId);
            throw new BadRequestException("User is already a member of this project");
        }

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("Target user not found: {}", request.userId());
                    return new ResourceNotFoundException("User", request.userId().toString());
                });

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(request.role())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);
        log.info("User {} added to project {} as {}", request.userId(), projectId, request.role());
        return MemberResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long projectId, Long requestingUserId) {
        log.info("User {} fetching members of projectId {}", requestingUserId, projectId);

        getProjectOrThrow(projectId);
        assertMembership(projectId, requestingUserId);

        List<MemberResponse> members = projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();

        log.debug("Found {} members in projectId {}", members.size(), projectId);
        return members;
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long targetUserId, Long requestingUserId) {
        log.info("User {} removing userId {} from projectId {}", requestingUserId, targetUserId, projectId);

        getProjectOrThrow(projectId);
        assertAdminRole(projectId, requestingUserId);

        if (targetUserId.equals(requestingUserId)) {
            log.warn("User {} tried to remove themselves from projectId {}", requestingUserId, projectId);
            throw new BadRequestException("Project admin cannot remove themselves. Transfer admin role first.");
        }

        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId)
                .orElseThrow(() -> {
                    log.warn("UserId {} is not a member of projectId {}", targetUserId, projectId);
                    return new BadRequestException("User is not a member of this project");
                });

        projectMemberRepository.delete(member);
        log.info("UserId {} removed from projectId {} by userId {}", targetUserId, projectId, requestingUserId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found: {}", projectId);
                    return new ResourceNotFoundException("Project", projectId.toString());
                });
    }

    private void assertMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByUserIdAndProjectId(userId, projectId)) {
            log.warn("Access denied — userId {} is not a member of projectId {}", userId, projectId);
            throw new BadRequestException("You are not a member of this project");
        }
    }

    private void assertAdminRole(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> {
                    log.warn("Access denied — userId {} not in projectId {}", userId, projectId);
                    return new BadRequestException("You are not a member of this project");
                });

        if (member.getRole() != Role.ADMIN) {
            log.warn("Access denied — userId {} is not ADMIN in projectId {}", userId, projectId);
            throw new BadRequestException("Only project admins can manage members");
        }
    }
}
