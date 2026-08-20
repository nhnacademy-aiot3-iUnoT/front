package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.AlertType;

import java.time.LocalDateTime;

public record AlertInfoResponse(
        Long alertId,
        Long organizationId,
        String organizationName,
        AlertType alertType,
        String message,
        Boolean isChecked,
        LocalDateTime createdAt
){

}