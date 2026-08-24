package com.nhnacademy.front.report.controller;

import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import com.nhnacademy.front.report.client.ReportApiClient;
import com.nhnacademy.front.report.dto.AiSummaryStatus;
import com.nhnacademy.front.report.dto.ReportDoorResponse;
import com.nhnacademy.front.report.dto.ReportEnvironmentResponse;
import com.nhnacademy.front.report.dto.ReportInfoResponse;
import com.nhnacademy.front.report.dto.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    private static final LocalDate PERIOD_START = LocalDate.now()
            .with(java.time.DayOfWeek.MONDAY)
            .minusWeeks(1);

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ReportApiClient reportApiClient;

    @MockitoBean
    private ZoneApiClient zoneApiClient;

    @Test
    @DisplayName("환경 데이터가 있으면 표와 차트 영역이 함께 렌더링된다.")
    void weeklyReport_WhenEnvironmentExists_RendersEnvironmentSection() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithEnvironment());
        given(zoneApiClient.getZones(anyLong()))
                .willReturn(zones());

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("report/weekly"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("보관 환경")))
                // 차트는 이 data 속성의 JSON을 읽는다.
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-environments")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("env-chart-canvas")));
    }

    @Test
    @DisplayName("구역별 탭으로 나뉘고 구역 이름이 표시된다.")
    void weeklyReport_GroupsByZoneWithNames() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithEnvironment());
        given(zoneApiClient.getZones(anyLong()))
                .willReturn(zones());

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("냉장 보관실")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("실온 보관실")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zone-pane-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zone-pane-2")));
    }

    @Test
    @DisplayName("구역 이름 조회에 실패해도 구역 번호로 표시하며 화면은 렌더링된다.")
    void weeklyReport_WhenZoneLookupFails_FallsBackToZoneId() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithEnvironment());
        given(zoneApiClient.getZones(anyLong()))
                .willThrow(new RuntimeException("zone api down"));

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("구역 1")));
    }

    @Test
    @DisplayName("문 센서가 없는 구역은 개폐 0회가 아니라 센서 없음으로 표시한다.")
    void weeklyReport_WhenZoneHasNoDoorSensor_ShowsNoSensorMessage() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithEnvironment());
        given(zoneApiClient.getZones(anyLong()))
                .willReturn(zones());

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("이 구역에는 문 센서가 없습니다")));
    }

    @Test
    @DisplayName("임계값이 한쪽만 설정된 구역은 제한없음으로 표기한다.")
    void weeklyReport_WhenThresholdPartiallySet_RendersUnlimited() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithEnvironment());
        given(zoneApiClient.getZones(anyLong()))
                .willReturn(zones());

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("제한없음")));
    }

    @Test
    @DisplayName("환경 데이터가 없으면 환경 섹션 없이 렌더링된다.")
    void weeklyReport_WhenNoEnvironment_RendersWithoutEnvironmentSection() throws Exception {
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willReturn(reportWithoutEnvironment());

        mockMvc.perform(get("/storages/{storageId}/reports/weekly", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("report/weekly"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("env-chart-canvas"))));
    }

    private ReportInfoResponse reportWithEnvironment() {
        ReportEnvironmentResponse temperature = new ReportEnvironmentResponse(
                1L, "TEMPERATURE", "°C",
                new BigDecimal("5.80"), new BigDecimal("3.90"), new BigDecimal("10.40"),
                new BigDecimal("2.00"), new BigDecimal("8.00"),
                3, 7,
                List.of(new ReportEnvironmentResponse.DailyPointResponse(
                        PERIOD_START, new BigDecimal("4.80"), new BigDecimal("3.90"), new BigDecimal("5.60"))));

        // 상한만 설정된 구역. 화면에 '제한없음 ~ 30.0'으로 나와야 한다.
        ReportEnvironmentResponse roomTemperature = new ReportEnvironmentResponse(
                2L, "TEMPERATURE", "°C",
                new BigDecimal("24.20"), new BigDecimal("21.80"), new BigDecimal("27.40"),
                null, new BigDecimal("30.00"),
                0, 7,
                List.of(new ReportEnvironmentResponse.DailyPointResponse(
                        PERIOD_START, new BigDecimal("23.40"), new BigDecimal("21.80"), new BigDecimal("25.10"))));

        ReportDoorResponse door = new ReportDoorResponse(
                1L, 144L, 255L, 7,
                List.of(new ReportDoorResponse.DailyPointResponse(PERIOD_START, 12L, 18L)));

        return report(List.of(temperature, roomTemperature), List.of(door));
    }

    private ReportInfoResponse reportWithoutEnvironment() {
        return report(List.of(), List.of());
    }

    private List<ZoneInfoResponse> zones() {
        return List.of(
                new ZoneInfoResponse(1L, 1L, "냉장 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL),
                new ZoneInfoResponse(2L, 1L, "실온 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL));
    }

    private ReportInfoResponse report(
            List<ReportEnvironmentResponse> environments,
            List<ReportDoorResponse> doors
    ) {
        return new ReportInfoResponse(
                1L, 1L, 1L,
                ReportType.WEEKLY,
                PERIOD_START, PERIOD_START.plusDays(6),
                "요약 내용",
                AiSummaryStatus.COMPLETED,
                LocalDateTime.now(),
                List.of(),
                environments,
                doors);
    }
}
