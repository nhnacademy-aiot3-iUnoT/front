package com.nhnacademy.front.inventory.dto.response;

public record StockThresholdInfoResponse(
        Long stockThresholdId,
        Long medicinePackageUnitId,
        Long storageId,
        String productName,
        String packUnit,
        String organizationName,
        String storageName,
        Integer stockThreshold,
        Boolean isActive
){

}
