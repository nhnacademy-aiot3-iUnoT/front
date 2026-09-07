package com.nhnacademy.front.organization.service;

import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.client.MemberDepartmentApiClient;
import com.nhnacademy.front.organization.client.DepartmentTelegramChatApiClient;
import com.nhnacademy.front.organization.client.StorageDepartmentApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentPageService {
    private final DepartmentApiClient departmentApiClient;
    private final OrganizationMemberApiClient memberApiClient;
    private final MemberDepartmentApiClient memberDepartmentApiClient;
    private final StorageDepartmentApiClient storageDepartmentApiClient;
    private final DepartmentTelegramChatApiClient departmentTelegramChatApiClient;

    public DepartmentPageResponse getDepartmentPage(Long departmentId) {
        DepartmentInfoResponse department = departmentApiClient.getDepartment(departmentId);
        OrganizationMemberRoleResponse role = memberApiClient.getRole();
        List<StorageDepartmentResponse> storages = storageDepartmentApiClient.getStorages(departmentId);
        List<OrganizationMemberResponse> members = memberDepartmentApiClient.getMembers(departmentId);
        // 단톡방 연결 API는 OWNER/BOSS 전용이라 일반 조직원은 조회하지 않는다.
        DepartmentTelegramChatResponse telegramChat = role.role() == OrganizationRole.ORG_MEMBER
                ? null
                : departmentTelegramChatApiClient.getTelegramChat(departmentId);

        return new DepartmentPageResponse(department, role.role(), storages, members, telegramChat);
    }
}
