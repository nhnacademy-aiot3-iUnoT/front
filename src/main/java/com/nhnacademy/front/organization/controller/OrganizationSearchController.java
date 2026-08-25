package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.dto.request.MemberByEmailRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organizations/search")
public class OrganizationSearchController {
    private final OrganizationMemberApiClient memberApiClient;
    private final StorageApiClient storageApiClient;

    /**
     * 조직원 email로 검색
     */
    @GetMapping("/members")
    public List<OrganizationMemberResponse> searchMembers(@RequestParam(defaultValue = "") String email) {
        return memberApiClient.searchMembers(new MemberByEmailRequest(email));
    }

    /**
     * 조직에 있는 부서 이름으로 검색
     */
    @GetMapping("/storages")
    public List<StorageInfoResponse> searchStorages(@RequestParam(defaultValue = "") String name) {
        return storageApiClient.searchStorages(name);
    }
}
