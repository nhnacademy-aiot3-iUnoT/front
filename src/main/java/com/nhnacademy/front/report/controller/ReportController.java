package com.nhnacademy.front.report.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import com.nhnacademy.front.report.client.ReportApiClient;
import com.nhnacademy.front.report.dto.ReportDoorResponse;
import com.nhnacademy.front.report.dto.ReportEnvironmentResponse;
import com.nhnacademy.front.report.dto.ReportInfoResponse;
import com.nhnacademy.front.report.dto.ZoneEnvironmentView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/storages/{storageId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApiClient reportApiClient;
    private final ZoneApiClient zoneApiClient;
    private final ObjectMapper objectMapper;

    @GetMapping("/weekly")
    public String weeklyReport(
            @PathVariable Long storageId,
            @RequestParam(name = "periodStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            Model model
    ) {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);

        // 날짜 파라미터가 없거나 지난 주보다 미래 날짜인 경우 지난 주 월요일로 제한
        if (periodStart == null || periodStart.isAfter(lastMonday)) {
            periodStart = lastMonday;
        } else {
            // 사용자가 선택한 날짜가 월요일이 아니더라도 해당 주의 월요일로 자동 보정
            periodStart = periodStart.with(DayOfWeek.MONDAY);
        }

        try {
            ReportInfoResponse report = reportApiClient.getWeeklyReport(storageId, periodStart);
            model.addAttribute("report", report);
            model.addAttribute("environmentJson", toEnvironmentJson(report));
            model.addAttribute("zoneEnvironments", toZoneEnvironments(storageId, report));
        } catch (Exception e) {
            // 리포트가 아직 생성되지 않은 상태 -> report = null로 뷰 전달
            log.info("주간 리포트가 아직 생성되지 않았습니다 (storageId={}, periodStart={})", storageId, periodStart);
            model.addAttribute("report", null);
        }

        model.addAttribute("storageId", storageId);
        model.addAttribute("currentMonday", periodStart);
        model.addAttribute("lastMonday", lastMonday);
        model.addAttribute("lastSunday", lastMonday.plusDays(6));
        model.addAttribute("prevMonday", periodStart.minusWeeks(1));
        model.addAttribute("nextMonday", periodStart.plusWeeks(1));
        model.addAttribute("hasFuture", periodStart.isBefore(lastMonday));
        model.addAttribute("isLatestWeek", periodStart.isEqual(lastMonday));

        return "report/weekly";
    }

    @PostMapping("/weekly")
    public String createWeeklyReport(
            @PathVariable Long storageId,
            @RequestParam(name = "periodStart")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart
    ) {
        try {
            reportApiClient.createWeeklyReport(storageId, periodStart);
        } catch (Exception e) {
            log.error("주간 리포트 생성 실패: storageId={}, periodStart={}", storageId, periodStart, e);
        }

        return "redirect:/storages/" + storageId + "/reports/weekly?periodStart=" + periodStart;
    }

    @PostMapping("/{reportId}/ai-summary/retry")
    public String retryAiSummary(
            @PathVariable Long storageId,
            @PathVariable Long reportId,
            @RequestParam(name = "periodStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart
    ) {
        try {
            reportApiClient.retryAiSummary(storageId, reportId);
        } catch (Exception e) {
            log.error("AI 요약 생성을 재시도하는 도중 오류가 발생했습니다: {}", reportId, e);
        }

        if (periodStart != null) {
            return "redirect:/storages/" + storageId + "/reports/weekly?periodStart=" + periodStart;
        }
        return "redirect:/storages/" + storageId + "/reports/weekly";
    }

    private List<ZoneEnvironmentView> toZoneEnvironments(Long storageId, ReportInfoResponse report) {
        if (report == null || report.environments() == null || report.environments().isEmpty()) {
            return List.of();
        }

        Map<Long, String> zoneNames = loadZoneNames(storageId);

        Map<Long, ReportDoorResponse> doorsByZone = (report.doors() == null)
                ? Map.of()
                : report.doors().stream()
                .collect(Collectors.toMap(ReportDoorResponse::zoneId, Function.identity(), (a, b) -> a));

        return report.environments().stream()
                .collect(Collectors.groupingBy(
                        ReportEnvironmentResponse::zoneId,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new ZoneEnvironmentView(
                        entry.getKey(),
                        zoneNames.getOrDefault(entry.getKey(), "구역 " + entry.getKey()),
                        entry.getValue(),
                        doorsByZone.get(entry.getKey())))
                .sorted(Comparator.comparing(ZoneEnvironmentView::zoneId))
                .toList();
    }

    private Map<Long, String> loadZoneNames(Long storageId) {
        try {
            List<ZoneInfoResponse> zones = zoneApiClient.getZones(storageId);

            if (zones == null) {
                return Map.of();
            }

            return zones.stream()
                    .collect(Collectors.toMap(ZoneInfoResponse::zoneId, ZoneInfoResponse::name, (a, b) -> a));
        } catch (Exception e) {
            log.warn("구역 이름을 불러오지 못해 구역 번호로 표시합니다. storageId={}", storageId, e);
            return Map.of();
        }
    }

    private String toEnvironmentJson(ReportInfoResponse report) {
        if (report == null || report.environments() == null) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(report.environments());
        } catch (JsonProcessingException e) {
            log.warn("환경 차트 데이터를 JSON으로 변환하지 못했습니다. reportId={}", report.reportId(), e);
            return "[]";
        }
    }
}
