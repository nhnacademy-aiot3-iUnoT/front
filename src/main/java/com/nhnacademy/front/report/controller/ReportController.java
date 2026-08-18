package com.nhnacademy.front.report.controller;

import com.nhnacademy.front.report.client.ReportApiClient;
import com.nhnacademy.front.report.dto.ReportInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApiClient reportApiClient;

    @GetMapping("/weekly")
    public String weeklyReport(
            @RequestParam(name = "periodStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            Model model
    ) {
        LocalDate todayMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        // 날짜 파라미터가 없거나 미래 날짜인 경우 이번 주 월요일로 제한
        if (periodStart == null || periodStart.isAfter(todayMonday)) {
            periodStart = todayMonday;
        } else {
            // 사용자가 선택한 날짜가 월요일이 아니더라도 해당 주의 월요일로 자동 보정
            periodStart = periodStart.with(DayOfWeek.MONDAY);
        }

        try {
            ReportInfoResponse report = reportApiClient.getWeeklyReport(periodStart);
            model.addAttribute("report", report);
        } catch (Exception e) {
            // 리포트가 아직 생성되지 않은 상태 -> report = null로 뷰 전달
            log.info("주간 리포트가 아직 생성되지 않았습니다 (periodStart={})", periodStart);
            model.addAttribute("report", null);
        }

        model.addAttribute("currentMonday", periodStart);
        model.addAttribute("todayMonday", todayMonday);
        model.addAttribute("prevMonday", periodStart.minusWeeks(1));
        model.addAttribute("nextMonday", periodStart.plusWeeks(1));
        model.addAttribute("hasFuture", periodStart.isBefore(todayMonday));
        model.addAttribute("isCurrentWeek", periodStart.isEqual(todayMonday));

        return "report/weekly";
    }

    @PostMapping("/weekly")
    public String createWeeklyReport(
            @RequestParam(name = "periodStart")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart
    ) {
        try {
            reportApiClient.createWeeklyReport(periodStart);
        } catch (Exception e) {
            log.error("주간 리포트 생성 실패: periodStart={}", periodStart, e);
        }

        return "redirect:/reports/weekly?periodStart=" + periodStart;
    }

    @PostMapping("/{reportId}/ai-summary/retry")
    public String retryAiSummary(
            @PathVariable Long reportId,
            @RequestParam(name = "periodStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart
    ) {
        try {
            reportApiClient.retryAiSummary(reportId);
        } catch (Exception e) {
            log.error("AI 요약 생성을 재시도하는 도중 오류가 발생했습니다: {}", reportId, e);
        }

        if (periodStart != null) {
            return "redirect:/reports/weekly?periodStart=" + periodStart;
        }
        return "redirect:/reports/weekly";
    }
}
