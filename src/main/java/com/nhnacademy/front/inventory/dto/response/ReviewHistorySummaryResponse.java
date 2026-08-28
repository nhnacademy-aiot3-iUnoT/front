package com.nhnacademy.front.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewHistorySummaryResponse(
        Long environmentReviewId,
        Long inventoryId,
        Long medicineId,
        Long medicinePackageUnitId,
        String productName,
        String packUnit,
        LocalDateTime createdAt,
        UUID reviewerId,
        Boolean isOut
){

}