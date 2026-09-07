package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.organization.client.DepartmentTelegramChatApiClient;
import com.nhnacademy.front.organization.dto.request.DepartmentTelegramChatRegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 부서_단톡방
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/departments/{department-id}/telegram-chat")
public class DepartmentTelegramChatController {
    private final DepartmentTelegramChatApiClient departmentTelegramChatApiClient;

    /**
     * 단톡방 연결 및 교체
     */
    @PutMapping
    public ResponseEntity<Map<String, String>> registerTelegramChat(
            @PathVariable("department-id") Long departmentId,
            @RequestBody @Valid DepartmentTelegramChatRegisterRequest request
    ) {
        departmentTelegramChatApiClient.registerTelegramChat(departmentId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 단톡방 연결 해제
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> unlinkTelegramChat(
            @PathVariable("department-id") Long departmentId
    ) {
        departmentTelegramChatApiClient.unlinkTelegramChat(departmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 이 컨트롤러는 화면이 아니라 fetch로 호출된다.
     * 전역 핸들러는 에러 화면(HTML)을 200으로 돌려줘서 JS가 실패를 감지하지 못하므로 여기서 직접 처리한다.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException e) {
        String message = e.getMessage() != null ? e.getMessage() : "요청 처리에 실패했습니다.";

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
