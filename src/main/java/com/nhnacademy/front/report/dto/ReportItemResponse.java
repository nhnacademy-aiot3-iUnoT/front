package com.nhnacademy.front.report.dto;

public record ReportItemResponse(
        Long reportItemId,
        ReportItemType reportItemType,
        Long medicinePackageUnitId,
        String medicineName,
        String packUnit,
        int quantity
) {}
