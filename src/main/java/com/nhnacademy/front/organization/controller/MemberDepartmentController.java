package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.MemberDepartmentApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 부서_조직원
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/departments/{department-id}/members")
public class MemberDepartmentController {
    private final MemberDepartmentApiClient memberDepartmentApiClient;

    /**
     * 부서에 조직원 추가
     */
    @PostMapping("/{member-id}")
    public ResponseEntity<Void> addMember(@PathVariable("department-id") Long departmentId,
                                          @PathVariable("member-id") Long memberId) {
        memberDepartmentApiClient.addMember(departmentId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 부서에서 조직원 삭제
     */
    @DeleteMapping("/{member-id}")
    public ResponseEntity<Void> removeMember(@PathVariable("department-id") Long departmentId,
                                             @PathVariable("member-id") Long memberId) {
        memberDepartmentApiClient.removeMember(departmentId, memberId);
        return ResponseEntity.noContent().build();
    }
}
