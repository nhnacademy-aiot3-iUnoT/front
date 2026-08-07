package com.nhnacademy.front.admin.dto.response;

import com.nhnacademy.front.admin.dto.OrganizationStatus;
import java.time.LocalDateTime;

public record OrgSearchResponse(
        Long id,
        String businessNumber,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt
){
}
