package com.nhnacademy.front.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReviewHistoryDetailResponse(
        Long environmentReviewId,
        Long inventoryId,
        Long organizationId,
        Long storageId,
        Long zoneId,
        Long medicineId,
        Long medicinePackageUnitId,
        String organizationName,
        String storageName,
        String zoneName,
        String productName,
        String packUnit,
        String lotNumber,
        LocalDateTime createdAt,
        UUID reviewerId,
        String reviewerName,
        Boolean isOut,
        Integer quantityAtReview,
        String memo,

        List<ReviewHistorySummaryResponse> inventoryReviewHistories,
        List<EnvironmentEventItemResponse> environmentEvents
) {
}
