package com.nhnacademy.front.admin.dto.request;

import com.nhnacademy.front.admin.dto.OrganizationStatus;

public record OrgSearchRequest (
        OrganizationStatus status,
        String name
){
}
