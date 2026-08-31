package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.client.StockTransactionApiClient;
import com.nhnacademy.front.inventory.dto.TransactionType;
import com.nhnacademy.front.inventory.dto.response.StockTransactionResponse;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StockTransactionController {

    private static final String VIEW = "inventory/stock-transaction-list";

    private final StockTransactionApiClient stockTransactionApiClient;
    private final StorageApiClient storageApiClient;
    private final ZoneApiClient zoneApiClient;

    @GetMapping("/stock-transactions")
    public String getStockTransactions(
            @RequestParam(name = "storage-id", required = false) Long storageId,
            @RequestParam(name = "zone-id", required = false) Long zoneId,
            @RequestParam(name = "medicineName", required = false) String medicineName,
            @RequestParam(name = "transactionType", required = false) TransactionType transactionType,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model
    ) {
        List<StorageInfoResponse> storages = loadStorages();

        Long selectedStorageId = resolveStorageId(storageId, storages);
        List<ZoneInfoResponse> zones = loadZones(selectedStorageId);
        Long selectedZoneId = resolveZoneId(zoneId, zones);

        model.addAttribute("storages", storages);
        model.addAttribute("zones", zones);
        model.addAttribute("selectedStorageId", selectedStorageId);
        model.addAttribute("selectedZoneId", selectedZoneId);

        model.addAttribute("medicineName", medicineName);
        model.addAttribute("transactionType", transactionType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("transactionTypes", TransactionType.values());

        if (selectedZoneId == null) {
            model.addAttribute("transactions", List.of());
            return VIEW;
        }

        PageResponse<StockTransactionResponse> result =
                search(selectedZoneId, medicineName, transactionType, startDate, endDate, page, size);

        model.addAttribute("transactions", result.content());
        model.addAttribute("currentPage", result.page());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("totalElements", result.totalElements());
        model.addAttribute("pageSize", result.size());

        return VIEW;
    }

    private PageResponse<StockTransactionResponse> search(
            Long zoneId, String medicineName, TransactionType transactionType,
            LocalDate startDate, LocalDate endDate, int page, int size
    ) {
        try {
            return stockTransactionApiClient.search(
                    zoneId, medicineName, transactionType, startDate, endDate, page, size);
        } catch (Exception e) {
            log.warn("재고 변동 내역을 불러오지 못했습니다. zoneId={}", zoneId, e);
            return new PageResponse<>(List.of(), page, size, 0L, 0, true);
        }
    }

    private List<StorageInfoResponse> loadStorages() {
        try {
            List<StorageInfoResponse> storages = storageApiClient.getStorages();
            return (storages != null) ? storages : List.of();
        } catch (Exception e) {
            log.warn("저장소 목록을 불러오지 못했습니다.", e);
            return List.of();
        }
    }

    private List<ZoneInfoResponse> loadZones(Long storageId) {
        if (storageId == null) {
            return List.of();
        }

        try {
            List<ZoneInfoResponse> zones = zoneApiClient.getZones(storageId);
            return (zones != null) ? zones : List.of();
        } catch (Exception e) {
            log.warn("구역 목록을 불러오지 못했습니다. storageId={}", storageId, e);
            return List.of();
        }
    }

    private Long resolveStorageId(Long storageId, List<StorageInfoResponse> storages) {
        if (storages.isEmpty()) {
            return null;
        }

        boolean exists = storages.stream()
                .anyMatch(storage -> storage.storageId().equals(storageId));

        return exists ? storageId : storages.getFirst().storageId();
    }

    private Long resolveZoneId(Long zoneId, List<ZoneInfoResponse> zones) {
        if (zones.isEmpty()) {
            return null;
        }

        boolean exists = zones.stream()
                .anyMatch(zone -> zone.zoneId().equals(zoneId));

        return exists ? zoneId : zones.getFirst().zoneId();
    }
}
