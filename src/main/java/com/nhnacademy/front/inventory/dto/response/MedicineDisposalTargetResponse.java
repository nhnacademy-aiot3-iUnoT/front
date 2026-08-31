package com.nhnacademy.front.inventory.dto.response;

import java.time.LocalDate;

public record MedicineDisposalTargetResponse(
        Long inventoryId,
        String itemCode,
        String productName,
        String packUnit,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        Long storageId,
        String storageName,
        Long zoneId,
        String zoneName
) {
}