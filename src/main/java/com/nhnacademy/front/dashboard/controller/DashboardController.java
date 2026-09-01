package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.dashboard.client.DashboardApiClient;
import com.nhnacademy.front.dashboard.dto.response.DashboardDepartmentsResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardDepartmentsResponse.DepartmentOptionResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardEnvironmentResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardExpiringResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardSummaryResponse;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.StorageDepartmentApiClient;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.response.StorageDepartmentResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import com.nhnacademy.front.report.client.ReportApiClient;
import com.nhnacademy.front.report.dto.ReportInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/**
 * 메인 대시보드 화면
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final String DASHBOARD_VIEW = "dashboard/main";
    private static final int EXPIRING_ROW_SIZE = 5;

    /** 드롭다운에서 "조직 전체"를 고르면 이 값이 넘어온다. 서버에는 departmentId 없이 요청한다. */
    private static final long ORG_WIDE = 0L;

    private final DashboardApiClient dashboardApiClient;
    private final StorageDepartmentApiClient storageDepartmentApiClient;
    private final StorageApiClient storageApiClient;
    private final ReportApiClient reportApiClient;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) Long reportStorageId,
            Model model
    ) {
        DashboardDepartmentsResponse departments = departments();

        // 관리자가 "조직 전체"를 고른 경우 부서 없이(=조직 전체) 집계한다
        boolean orgWide = departmentId != null
                && departmentId == ORG_WIDE
                && departments.orgAdmin();

        Long selectedDepartmentId = orgWide
                ? null
                : resolveDepartmentId(departments, departmentId);

        model.addAttribute("departments", departments);
        model.addAttribute("orgWide", orgWide);
        model.addAttribute("selectedDepartmentId", selectedDepartmentId);
        model.addAttribute("selectedDepartmentName", departmentName(departments, selectedDepartmentId));
        model.addAttribute("organizationName", departments.organizationName());

        // 부서도 안 골랐고 조직 전체도 아니면 조회할 범위가 없다
        boolean hasScope = orgWide || selectedDepartmentId != null;

        if (hasScope) {
            addSummary(model, selectedDepartmentId);
            addExpiring(model, selectedDepartmentId);
        }

        model.addAttribute("hasScope", hasScope);

        List<StorageDepartmentResponse> storages = orgWide
                ? organizationStorages()
                : storages(selectedDepartmentId);
        Long selectedStorageId = resolveStorageId(storages, storageId);

        model.addAttribute("storages", storages);
        model.addAttribute("selectedStorageId", selectedStorageId);
        model.addAttribute("storageCount", storages.size());

        addEnvironment(model, selectedStorageId);

        // AI 리포트는 환경 현황과 독립적으로 저장소를 고를 수 있다
        Long selectedReportStorageId = resolveStorageId(storages, reportStorageId);
        model.addAttribute("selectedReportStorageId", selectedReportStorageId);

        addWeeklyReport(model, selectedReportStorageId);

        return DASHBOARD_VIEW;
    }

    /**
     * 부서 목록 조회
     */
    private DashboardDepartmentsResponse departments() {
        try {
            DashboardDepartmentsResponse departments = dashboardApiClient.getDepartments();

            return departments == null ? emptyDepartments() : departments;
        } catch (RuntimeException e) {
            log.error("부서 목록 조회에 실패했습니다.", e);

            return emptyDepartments();
        }
    }

    private DashboardDepartmentsResponse emptyDepartments() {
        return new DashboardDepartmentsResponse(false, null, List.of(), List.of());
    }

    private void addSummary(Model model, Long departmentId) {
        try {
            DashboardSummaryResponse summary = dashboardApiClient.getSummary(departmentId);
            model.addAttribute("summary", summary);
        } catch (RuntimeException e) {
            log.warn("대시보드 요약 조회에 실패했습니다. departmentId={}", departmentId, e);
            model.addAttribute("summary", null);
        }
    }

    private void addExpiring(Model model, Long departmentId) {
        try {
            DashboardExpiringResponse expiring =
                    dashboardApiClient.getExpiring(departmentId, EXPIRING_ROW_SIZE);
            model.addAttribute("expiring", expiring);
        } catch (RuntimeException e) {
            log.warn("임박 의약품 조회에 실패했습니다. departmentId={}", departmentId, e);
            model.addAttribute("expiring", null);
        }
    }

    private void addEnvironment(Model model, Long storageId) {
        if (storageId == null) {
            model.addAttribute("environment", null);
            return;
        }

        try {
            DashboardEnvironmentResponse environment = dashboardApiClient.getEnvironment(storageId);

            model.addAttribute("environment", environment);
            model.addAttribute("normalZoneCount", environment.countByStatus(EnvStatus.NORMAL));
            model.addAttribute("warningZoneCount", environment.countByStatus(EnvStatus.WARNING));
            model.addAttribute("dangerZoneCount", environment.countByStatus(EnvStatus.CRITICAL));
        } catch (RuntimeException e) {
            log.warn("환경 현황 조회에 실패했습니다. storageId={}", storageId, e);
            model.addAttribute("environment", null);
        }
    }

    private void addWeeklyReport(Model model, Long storageId) {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        model.addAttribute("reportPeriodStart", lastMonday);
        model.addAttribute("reportPeriodEnd", lastMonday.plusDays(6));

        if (storageId == null) {
            model.addAttribute("weeklyReport", null);
            return;
        }

        try {
            ReportInfoResponse report = reportApiClient.getWeeklyReport(storageId, lastMonday);
            model.addAttribute("weeklyReport", report);
        } catch (RuntimeException e) {
            log.warn("주간 리포트 조회에 실패했습니다. storageId={}", storageId, e);
            model.addAttribute("weeklyReport", null);
        }
    }

    private List<StorageDepartmentResponse> storages(Long departmentId) {
        if (departmentId == null) {
            return List.of();
        }

        try {
            List<StorageDepartmentResponse> storages =
                    storageDepartmentApiClient.getStorages(departmentId);

            return storages == null ? List.of() : storages;
        } catch (RuntimeException e) {
            log.warn("부서 담당 저장소 조회에 실패했습니다. departmentId={}", departmentId, e);
            return List.of();
        }
    }

    /** 조직 전체 조회일 때는 부서와 무관하게 조직의 모든 저장소를 쓴다. */
    private List<StorageDepartmentResponse> organizationStorages() {
        try {
            List<StorageInfoResponse> storages = storageApiClient.getStorages();

            if (storages == null) {
                return List.of();
            }

            return storages.stream()
                    .map(storage -> new StorageDepartmentResponse(
                            storage.storageId(), storage.name(), storage.status()))
                    .toList();
        } catch (RuntimeException e) {
            log.warn("조직 저장소 목록 조회에 실패했습니다.", e);

            return List.of();
        }
    }

    /** 요청 파라미터가 없거나 접근 가능한 부서가 아니면 내 첫 부서를 고른다. */
    private Long resolveDepartmentId(DashboardDepartmentsResponse departments, Long requested) {
        List<DepartmentOptionResponse> selectable = selectableDepartments(departments);

        if (requested != null
                && selectable.stream().anyMatch(option -> option.departmentId().equals(requested))) {
            return requested;
        }

        return selectable.isEmpty() ? null : selectable.getFirst().departmentId();
    }

    private List<DepartmentOptionResponse> selectableDepartments(DashboardDepartmentsResponse departments) {
        List<DepartmentOptionResponse> mine = departments.myDepartments() == null
                ? List.of() : departments.myDepartments();
        List<DepartmentOptionResponse> others = departments.otherDepartments() == null
                ? List.of() : departments.otherDepartments();

        return Stream.concat(mine.stream(), others.stream()).toList();
    }

    private Long resolveStorageId(List<StorageDepartmentResponse> storages, Long requested) {
        if (storages.isEmpty()) {
            return null;
        }

        if (requested != null
                && storages.stream().anyMatch(storage -> storage.storageId().equals(requested))) {
            return requested;
        }

        return storages.getFirst().storageId();
    }

    private String departmentName(DashboardDepartmentsResponse departments, Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return selectableDepartments(departments).stream()
                .filter(option -> option.departmentId().equals(departmentId))
                .map(DepartmentOptionResponse::name)
                .findFirst()
                .orElse(null);
    }

}
