package com.nhnacademy.front.medicine.dto.response;

public record MedicineDetailResponse(

        Long medicineId,
        Long packageUnitId,
        String itemCode,
        String productName,
        String companyName,
        String storageMethod,
        String validityPeriod,
        String packUnit,
        String narcoticKindCode


) {
}
