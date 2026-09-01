package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.dashboard.client.DashboardApiClient;
import com.nhnacademy.front.dashboard.dto.response.DashboardDepartmentsResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardDepartmentsResponse.DepartmentOptionResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardEnvironmentResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardEnvironmentResponse.SensorEnvironmentResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardEnvironmentResponse.ZoneEnvironmentResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardExpiringResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardExpiringResponse.ExpiringItemResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardSummaryResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardSummaryResponse.MetricResponse;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.StorageDepartmentApiClient;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.StorageStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;
import com.nhnacademy.front.organization.dto.response.StorageDepartmentResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import com.nhnacademy.front.report.client.ReportApiClient;
import com.nhnacademy.front.report.dto.AiSummaryStatus;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private DashboardApiClient dashboardApiClient;

    @MockitoBean
    private StorageDepartmentApiClient storageDepartmentApiClient;

    @MockitoBean
    private StorageApiClient storageApiClient;

    @MockitoBean
    private ReportApiClient reportApiClient;

    @Test
    @DisplayName("메인 화면에 부서 KPI, 구역 센서 현황, 임박 의약품, AI 요약이 모두 보인다.")
    void dashboard_RendersAllWidgets() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE),
                new StorageDepartmentResponse(101L, "약제부 조제실", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willReturn(environment());
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/main"))
                .andExpect(model().attribute("selectedDepartmentId", 10L))
                .andExpect(model().attribute("selectedStorageId", 100L))
                // 조직 / 부서
                .andExpect(content().string(containsString("NHN 메디컬센터")))
                .andExpect(content().string(containsString("약제부 · 조제파트")))
                .andExpect(content().string(containsString("조직 관리자")))
                // KPI
                .andExpect(content().string(containsString("1,284")))
                .andExpect(content().string(containsString("142개")))
                // 구역 센서
                .andExpect(content().string(containsString("냉장 보관실 A")))
                .andExpect(content().string(containsString("10.4℃")))
                .andExpect(content().string(containsString("위험")))
                .andExpect(content().string(containsString("문 상태")))
                .andExpect(content().string(containsString("열림")))
                // 임박 의약품
                .andExpect(content().string(containsString("인슐린주 100IU/ml")))
                .andExpect(content().string(containsString("LOT-24A118")))
                // AI 리포트
                .andExpect(content().string(containsString("회전율이 전주보다 올랐습니다")))
                .andExpect(content().string(containsString("재고관리로 이동")));
    }

    @Test
    @DisplayName("소속 부서가 없으면 안내 문구만 보인다.")
    void dashboard_NoDepartment_RendersEmptyState() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(
                new DashboardDepartmentsResponse(false, "NHN 메디컬센터", List.of()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedDepartmentId", (Object) null))
                .andExpect(content().string(containsString("표시할 부서가 없습니다")));
    }

    @Test
    @DisplayName("위젯 조회가 실패해도 화면은 정상 렌더링된다.")
    void dashboard_WidgetFailure_StillRenders() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(anyLong())).willThrow(new RuntimeException("게이트웨이 오류"));
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willThrow(new RuntimeException("게이트웨이 오류"));
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willThrow(new RuntimeException("게이트웨이 오류"));
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class)))
                .willThrow(new RuntimeException("리포트 없음"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("summary", (Object) null))
                .andExpect(content().string(containsString("부서 종합 현황")))
                .andExpect(content().string(containsString("표시할 환경 정보가 없습니다")))
                .andExpect(content().string(containsString("리포트가 아직 생성되지 않았습니다")));
    }

    private DashboardDepartmentsResponse departments() {
        return new DashboardDepartmentsResponse(
                true, "NHN 메디컬센터",
                List.of(
                        new DepartmentOptionResponse(10L, "약제부 · 조제파트"),
                        new DepartmentOptionResponse(20L, "내과")
                )
        );
    }

    private DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                new MetricResponse(1284L, 32L, 142L, 12.4),
                new MetricResponse(968L, 51L, -52L, -5.1),
                new MetricResponse(17L, 0L, 3L, null),
                new MetricResponse(4L, 2L, 0L, null)
        );
    }

    private DashboardExpiringResponse expiring() {
        return new DashboardExpiringResponse(
                List.of(new ExpiringItemResponse(
                        1L, 2L, 3L, 7L, 100L, 11L,
                        "인슐린주 100IU/ml", "10ml", "NHN 메디컬센터",
                        "본원 냉장창고", "냉장 보관실 A",
                        "LOT-24A118", LocalDate.now().plusDays(3), 24)),
                4L, 17L
        );
    }

    private DashboardEnvironmentResponse environment() {
        return new DashboardEnvironmentResponse(
                100L, "본원 냉장창고", StorageStatus.ACTIVE,
                List.of(new ZoneEnvironmentResponse(
                        11L, "냉장 보관실 A", ZoneStatus.ACTIVE, EnvStatus.CRITICAL,
                        List.of(
                                // 현재 10.4℃ 로 상한 8.0 초과
                                new SensorEnvironmentResponse("TEMPERATURE", "℃", new BigDecimal("2.0"), new BigDecimal("8.0"), 10.4),
                                new SensorEnvironmentResponse("HUMIDITY", "%", new BigDecimal("35"), new BigDecimal("60"), 48.0)
                        ),
                        true))
        );
    }

    private ReportInfoResponse report() {
        LocalDate monday = LocalDate.now().minusWeeks(1);

        return new ReportInfoResponse(
                500L, 7L, 100L, ReportType.WEEKLY,
                monday, monday.plusDays(6),
                "이번 주 본원 냉장창고는 재고 회전율이 전주보다 올랐습니다.",
                AiSummaryStatus.COMPLETED,
                LocalDateTime.now(),
                List.of(), List.of(), List.of()
        );
    }

    @Test
    @DisplayName("부서 목록 조회가 실패해도 메인 화면은 에러 페이지로 떨어지지 않는다.")
    void dashboard_DepartmentsFailure_StillRenders() throws Exception {
        given(dashboardApiClient.getDepartments()).willThrow(new RuntimeException("게이트웨이 500"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/main"))
                .andExpect(content().string(containsString("표시할 부서가 없습니다")));
    }

    @Test
    @DisplayName("선택할 부서가 없으면 요약·임박 API를 아예 호출하지 않는다.")
    void dashboard_NoDepartment_SkipsWidgetCalls() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(
                new DashboardDepartmentsResponse(false, "NHN 메디컬센터", List.of()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        then(dashboardApiClient).should(never()).getSummary(any());
        then(dashboardApiClient).should(never()).getExpiring(any(), anyInt());
        then(dashboardApiClient).should(never()).getEnvironment(anyLong());
    }

    @Test
    @DisplayName("임계값이 없는 구역도 측정값이 있으면 온도·습도·조도가 모두 보인다.")
    void dashboard_NoThreshold_RendersMeasuredSensors() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE)
        ));
        // 임계값은 없지만 측정값은 들어오는 구역 (서버가 이미 합쳐서 내려준다)
        given(dashboardApiClient.getEnvironment(100L)).willReturn(
                new DashboardEnvironmentResponse(
                        100L, "본원 냉장창고", StorageStatus.ACTIVE,
                        List.of(new ZoneEnvironmentResponse(
                                11L, "상온 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                                List.of(
                                        new SensorEnvironmentResponse("temperature", "C", null, null, 22.4),
                                        new SensorEnvironmentResponse("humidity", "%", null, null, 53.0),
                                        new SensorEnvironmentResponse("illumination", "lux", null, null, 310.0)
                                ),
                                true))));
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("온도")))
                .andExpect(content().string(containsString("22.4C")))
                .andExpect(content().string(containsString("습도")))
                .andExpect(content().string(containsString("53%")))
                .andExpect(content().string(containsString("조도")))
                .andExpect(content().string(containsString("310lux")))
                .andExpect(content().string(containsString("문 상태")))
                .andExpect(content().string(containsString("열림")))
                .andExpect(content().string(containsString("임계값 미설정")));
    }

    @Test
    @DisplayName("AI 리포트는 환경 현황과 별도로 저장소를 고를 수 있다.")
    void dashboard_ReportStorageSelectedIndependently() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE),
                new StorageDepartmentResponse(101L, "약제부 조제실", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willReturn(environment());
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/?departmentId=10&storageId=100&reportStorageId=101"))
                .andExpect(status().isOk())
                // 환경 현황은 100번, AI 리포트는 101번 저장소
                .andExpect(model().attribute("selectedStorageId", 100L))
                .andExpect(model().attribute("selectedReportStorageId", 101L));

        then(reportApiClient).should().getWeeklyReport(eq(101L), any(LocalDate.class));
    }

    @Test
    @DisplayName("조도에 임계값을 등록하면 게이지 막대와 기준 범위, 이탈 판정이 함께 나온다.")
    void dashboard_IlluminationWithThreshold_RendersGauge() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE)
        ));
        // 조도에 임계값 100 ~ 500 lux 가 등록된 구역
        given(dashboardApiClient.getEnvironment(100L)).willReturn(
                new DashboardEnvironmentResponse(
                        100L, "본원 냉장창고", StorageStatus.ACTIVE,
                        List.of(new ZoneEnvironmentResponse(
                                11L, "상온 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                                List.of(new SensorEnvironmentResponse("ILLUMINATION", "lux", new BigDecimal("100"), new BigDecimal("500"), 310.0)),
                                null))));
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("조도")))
                .andExpect(content().string(containsString("310lux")))
                // 임계값이 있으므로 기준 범위가 나온다
                .andExpect(content().string(containsString("기준 100 ~ 500lux")))
                .andExpect(content().string(not(containsString("임계값 미설정"))));
    }

    @Test
    @DisplayName("서버가 위험으로 판정한 구역은 그대로 위험으로 표시된다.")
    void dashboard_ServerReportedCritical_IsShown() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willReturn(
                new DashboardEnvironmentResponse(
                        100L, "본원 냉장창고", StorageStatus.ACTIVE,
                        List.of(new ZoneEnvironmentResponse(
                                11L, "상온 보관실", ZoneStatus.ACTIVE, EnvStatus.CRITICAL,
                                List.of(new SensorEnvironmentResponse("ILLUMINATION", "lux", new BigDecimal("100"), new BigDecimal("500"), 820.0)),
                                null))));
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("820lux")))
                // 구역 상태는 서버가 준 envStatus 를 그대로 쓴다
                .andExpect(content().string(containsString("위험")));
    }

    @Test
    @DisplayName("현재값이 임계값을 벗어나도 서버가 정상이라 하면 화면이 임의로 위험 판정하지 않는다.")
    void dashboard_DoesNotInventStatusFromValue() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of(
                new StorageDepartmentResponse(100L, "본원 냉장창고", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willReturn(
                new DashboardEnvironmentResponse(
                        100L, "본원 냉장창고", StorageStatus.ACTIVE,
                        List.of(new ZoneEnvironmentResponse(
                                11L, "상온 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                                // 현재 820 lux 로 상한 500 을 넘지만 서버 판정은 정상
                                List.of(new SensorEnvironmentResponse("ILLUMINATION", "lux", new BigDecimal("100"), new BigDecimal("500"), 820.0)),
                                null))));
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("820lux")))
                // 화면이 값으로 등급을 만들지 않는다 (구역 카드가 위험으로 바뀌지 않음)
                .andExpect(content().string(not(containsString("상한 초과"))))
                .andExpect(content().string(not(containsString("card-status-start bg-red"))));
    }

    @Test
    @DisplayName("관리자가 '조직 전체'를 고르면 부서 없이 조직 전체로 집계하고 조직의 모든 저장소를 쓴다.")
    void dashboard_OrgWide_AggregatesWholeOrganization() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(null)).willReturn(summary());
        given(dashboardApiClient.getExpiring(any(), anyInt())).willReturn(expiring());
        given(storageApiClient.getStorages()).willReturn(List.of(
                new StorageInfoResponse(100L, 7L, "NHN 메디컬센터", "본원 냉장창고", StorageStatus.ACTIVE),
                new StorageInfoResponse(200L, 7L, "NHN 메디컬센터", "내과 창고", StorageStatus.ACTIVE)
        ));
        given(dashboardApiClient.getEnvironment(100L)).willReturn(environment());
        given(reportApiClient.getWeeklyReport(anyLong(), any(LocalDate.class))).willReturn(report());

        mockMvc.perform(get("/?departmentId=0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgWide", true))
                .andExpect(model().attribute("selectedDepartmentId", (Object) null))
                .andExpect(model().attribute("hasScope", true))
                .andExpect(model().attribute("storageCount", 2))
                .andExpect(content().string(containsString("조직 전체 저장소")))
                .andExpect(content().string(containsString("모든 부서 합산")))
                .andExpect(content().string(containsString("1,284")));

        // 부서 없이(=조직 전체) 호출한다
        then(dashboardApiClient).should().getSummary(null);
        then(dashboardApiClient).should().getExpiring(eq(null), anyInt());
        // 부서 담당 저장소 API 는 쓰지 않는다
        then(storageDepartmentApiClient).should(never()).getStorages(anyLong());
    }

    @Test
    @DisplayName("일반 조직원에게는 '조직 전체' 항목이 없고, 값을 넣어도 무시된다.")
    void dashboard_OrgWide_NotAvailableForMember() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(
                new DashboardDepartmentsResponse(false, "NHN 메디컬센터",
                        List.of(new DepartmentOptionResponse(10L, "약제부 · 조제파트"))));
        given(dashboardApiClient.getSummary(10L)).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(10L)).willReturn(List.of());

        mockMvc.perform(get("/?departmentId=0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgWide", false))
                // 조직 전체가 거부되고 내 첫 부서로 되돌아간다
                .andExpect(model().attribute("selectedDepartmentId", 10L))
                .andExpect(content().string(not(containsString("모든 부서 합산"))));

        then(storageApiClient).should(never()).getStorages();
    }

    @Test
    @DisplayName("관리자 드롭다운에는 조직 전체와 조직의 모든 부서가 함께 나열된다.")
    void dashboard_AdminDropdown_ListsOrgWideAndEveryDepartment() throws Exception {
        given(dashboardApiClient.getDepartments()).willReturn(departments());
        given(dashboardApiClient.getSummary(anyLong())).willReturn(summary());
        given(dashboardApiClient.getExpiring(anyLong(), anyInt())).willReturn(expiring());
        given(storageDepartmentApiClient.getStorages(anyLong())).willReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // 합산 항목
                .andExpect(content().string(containsString("모든 부서 합산")))
                .andExpect(content().string(containsString("departmentId=0")))
                // 부서별 항목 (내 소속 + 그 외 구분 없이 전부)
                .andExpect(content().string(containsString("약제부 · 조제파트")))
                .andExpect(content().string(containsString("내과")))
                .andExpect(content().string(containsString("departmentId=10")))
                .andExpect(content().string(containsString("departmentId=20")));
    }
}
