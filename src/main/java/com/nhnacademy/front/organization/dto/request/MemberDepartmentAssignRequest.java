package com.nhnacademy.front.organization.dto.request;

import java.util.List;

public record MemberDepartmentAssignRequest(
        List<Long> departmentIds
) {
}
