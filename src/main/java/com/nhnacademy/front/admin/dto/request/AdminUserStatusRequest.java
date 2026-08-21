package com.nhnacademy.front.admin.dto.request;

import com.nhnacademy.front.admin.dto.AccountStatusAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserStatusRequest(
        @NotNull(message = "변경할 상태를 선택해주세요.")
        AccountStatusAction action,

        @NotBlank(message = "상태 변경 사유를 입력해주세요.")
        @Size(max = 200, message = "변경 사유는 200자 이내로 입력해주세요.")
        String reason
) {
    public AdminUserStatusRequest() {
        this(null, "");
    }
}
