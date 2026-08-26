package com.nhnacademy.front.inventory.dto.response;

public record MedicineOutboundTargetResponse(
        Long inventoryId,
        Long medicinePackageUnitId,
        String itemCode,
        String productName,
        String packUnit,
        Integer availableQuantity,
        Long storageId,
        String storageName,
        Long zoneId,
        String zoneName
) {
}