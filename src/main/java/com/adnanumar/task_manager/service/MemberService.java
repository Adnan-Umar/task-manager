package com.adnanumar.task_manager.service;

import com.adnanumar.task_manager.dto.request.AddMemberRequest;
import com.adnanumar.task_manager.dto.response.MemberResponse;

import java.util.List;

public interface MemberService {

    MemberResponse addMember(Long projectId, AddMemberRequest request, Long requestingUserId);

    List<MemberResponse> getMembers(Long projectId, Long requestingUserId);

    void removeMember(Long projectId, Long targetUserId, Long requestingUserId);
}
