package com.nhnacademy.front.inventory.dto.response;

import com.nhnacademy.front.inventory.dto.ManagementStatus;

import java.time.LocalDate;

public record InventoryDetailResponse(

        Long inventoryId,
        Long zoneId,
        String zoneName,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        ManagementStatus managementStatus


) {
}
