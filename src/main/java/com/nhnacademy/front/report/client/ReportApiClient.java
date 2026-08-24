package com.nhnacademy.front.report.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.report.dto.ReportCreateRequest;
import com.nhnacademy.front.report.dto.ReportInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReportApiClient {

    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public ReportInfoResponse getWeeklyReport(Long storageId, LocalDate periodStart) {
        return gatewayClient.get(
                CORE_SERVICE + "/storages/" + storageId + "/reports/weekly?periodStart=" + periodStart,
                ReportInfoResponse.class
        );
    }

    public void createWeeklyReport(Long storageId, LocalDate periodStart) {
        gatewayClient.post(
                CORE_SERVICE + "/storages/" + storageId + "/reports/weekly",
                new ReportCreateRequest(periodStart),
                ReportInfoResponse.class
        );
    }

    public ReportInfoResponse getReport(Long storageId, Long reportId) {
        return gatewayClient.get(
                CORE_SERVICE + "/storages/" + storageId + "/reports/" + reportId,
                ReportInfoResponse.class
        );
    }

    public void retryAiSummary(Long storageId, Long reportId) {
        gatewayClient.post(CORE_SERVICE + "/storages/" + storageId + "/reports/" + reportId + "/ai-summary/retry");
    }
}
