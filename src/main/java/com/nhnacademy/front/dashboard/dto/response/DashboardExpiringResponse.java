package com.nhnacademy.front.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DashboardExpiringResponse(
        List<ExpiringItemResponse> items,
        long totalCount,
        long within7Count,
        long within30Count
) {
    public record ExpiringItemResponse(
            Long inventoryId,
            Long medicineId,
            Long medicinePackageUnitId,
            Long organizationId,
            Long storageId,
            Long zoneId,
            String medicineName,
            String packUnitName,
            String organizationName,
            String storageName,
            String zoneName,
            String lotNumber,
            LocalDate expirationDate,
            Integer currentQuantity
    ) {
        /** 남은 일수. 이미 지났으면 음수가 된다. */
        public long remainingDays() {
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        }

        public boolean urgent() {
            return remainingDays() <= 7L;
        }

        /** 남았으면 D-3, 이미 지났으면 D+2 로 표기한다. */
        public String ddayText() {
            long days = remainingDays();

            return days >= 0L ? "D-" + days : "D+" + Math.abs(days);
        }
    }
}
