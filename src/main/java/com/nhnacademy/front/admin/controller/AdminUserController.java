package com.nhnacademy.front.admin.controller;

import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountPasswordRequest;
import com.nhnacademy.front.admin.client.AdminApiClient;
import com.nhnacademy.front.admin.dto.AccountStatus;
import com.nhnacademy.front.admin.dto.AccountStatusAction;
import com.nhnacademy.front.admin.dto.AdminUserSort;
import com.nhnacademy.front.admin.dto.request.AdminUserCreateRequest;
import com.nhnacademy.front.admin.dto.request.AdminUserStatusRequest;
import com.nhnacademy.front.admin.dto.response.AdminUserResponse;
import com.nhnacademy.front.global.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 20, 50);

    private final AdminApiClient adminApiClient;

    @GetMapping
    public String userList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) AccountRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "CREATED_DESC") AdminUserSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        List<AdminUserResponse> allUsers = adminApiClient.getUsers();
        PageResponse<AdminUserResponse> users = buildPage(
                allUsers, keyword, role, status, sort, page, size
        );

        model.addAttribute("users", users);
        model.addAttribute("pageNumbers", pageNumbers(users.page(), users.totalPages()));
        model.addAttribute("roles", AccountRole.values());
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("sortTypes", AdminUserSort.values());
        model.addAttribute("pageSizes", ALLOWED_PAGE_SIZES.stream().sorted().toList());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("totalUserCount", allUsers.size());
        model.addAttribute("adminCount", countByRole(allUsers, AccountRole.ADMIN));
        model.addAttribute("activeCount", countByStatus(allUsers, AccountStatus.ACTIVE));

        return "admin/user-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("createRequest")) {
            model.addAttribute("createRequest", new AdminUserCreateRequest());
        }
        return "admin/user-create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("createRequest") AdminUserCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/user-create";
        }

        adminApiClient.createAdmin(request);
        redirectAttributes.addFlashAttribute("successMessage", "관리자 계정이 생성되었습니다.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{uuid}")
    public String detail(@PathVariable UUID uuid, Model model) {
        populateDetailModel(uuid, model);
        return "admin/user-detail";
    }

    @PutMapping("/{uuid}/name")
    public String updateName(
            @PathVariable UUID uuid,
            @Valid @ModelAttribute("nameRequest") UpdateAccountNameRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateDetailModel(uuid, model);
            return "admin/user-detail";
        }

        adminApiClient.updateName(uuid, request);
        redirectAttributes.addFlashAttribute("successMessage", "회원 이름을 수정했습니다.");
        return "redirect:/admin/users/" + uuid;
    }

    @PutMapping("/{uuid}/password")
    public String updatePassword(
            @PathVariable UUID uuid,
            @Valid @ModelAttribute("passwordRequest") UpdateAccountPasswordRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateDetailModel(uuid, model);
            return "admin/user-detail";
        }

        adminApiClient.updatePassword(uuid, request);
        redirectAttributes.addFlashAttribute("successMessage", "회원 비밀번호를 재설정했습니다.");
        return "redirect:/admin/users/" + uuid;
    }

    @PutMapping("/{uuid}/status")
    public String changeStatus(
            @PathVariable UUID uuid,
            @Valid @ModelAttribute("statusRequest") AdminUserStatusRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateDetailModel(uuid, model);
            return "admin/user-detail";
        }

        adminApiClient.changeStatus(uuid, request);
        redirectAttributes.addFlashAttribute("successMessage", "회원 상태를 변경했습니다.");
        return "redirect:/admin/users/" + uuid;
    }

    private PageResponse<AdminUserResponse> buildPage(
            List<AdminUserResponse> allUsers,
            String keyword,
            AccountRole role,
            AccountStatus status,
            AdminUserSort sort,
            int requestedPage,
            int requestedSize
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        int size = ALLOWED_PAGE_SIZES.contains(requestedSize) ? requestedSize : DEFAULT_PAGE_SIZE;

        List<AdminUserResponse> filteredUsers = allUsers.stream()
                .filter(user -> matchesKeyword(user, normalizedKeyword))
                .filter(user -> role == null || user.accountRole() == role)
                .filter(user -> status == null || user.accountStatus() == status)
                .sorted(userComparator(sort))
                .toList();

        int totalPages = (int) Math.ceil((double) filteredUsers.size() / size);
        int page = totalPages == 0
                ? 0
                : Math.min(Math.max(requestedPage, 0), totalPages - 1);
        int start = Math.min(page * size, filteredUsers.size());
        int end = Math.min(start + size, filteredUsers.size());

        return new PageResponse<>(
                filteredUsers.subList(start, end),
                page,
                size,
                filteredUsers.size(),
                totalPages,
                totalPages == 0 || page == totalPages - 1
        );
    }

    private boolean matchesKeyword(AdminUserResponse user, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(user.name(), keyword) || containsIgnoreCase(user.email(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Comparator<AdminUserResponse> userComparator(AdminUserSort sort) {
        Comparator<AdminUserResponse> comparator = switch (sort) {
            case CREATED_ASC -> Comparator.comparing(
                    AdminUserResponse::createdAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case NAME_ASC -> Comparator.comparing(
                    AdminUserResponse::name,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case EMAIL_ASC -> Comparator.comparing(
                    AdminUserResponse::email,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case ROLE_ASC -> Comparator.comparing(AdminUserResponse::accountRole);
            case STATUS_ASC -> Comparator.comparing(AdminUserResponse::accountStatus);
            case CREATED_DESC -> Comparator.comparing(
                    AdminUserResponse::createdAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        };

        return comparator.thenComparing(user -> user.uuid().toString());
    }

    private List<Integer> pageNumbers(int currentPage, int totalPages) {
        if (totalPages == 0) {
            return List.of();
        }

        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages - 1, start + 4);
        start = Math.max(0, end - 4);

        List<Integer> pages = new ArrayList<>();
        for (int page = start; page <= end; page++) {
            pages.add(page);
        }
        return pages;
    }

    private long countByRole(List<AdminUserResponse> users, AccountRole role) {
        return users.stream().filter(user -> user.accountRole() == role).count();
    }

    private long countByStatus(List<AdminUserResponse> users, AccountStatus status) {
        return users.stream().filter(user -> user.accountStatus() == status).count();
    }

    private void populateDetailModel(UUID uuid, Model model) {
        AdminUserResponse user = adminApiClient.getUser(uuid);
        model.addAttribute("user", user);
        model.addAttribute("statusActions", allowedStatusActions(user.accountStatus()));

        if (!model.containsAttribute("nameRequest")) {
            model.addAttribute("nameRequest", new UpdateAccountNameRequest(user.name()));
        }
        if (!model.containsAttribute("passwordRequest")) {
            model.addAttribute("passwordRequest", new UpdateAccountPasswordRequest(""));
        }
        if (!model.containsAttribute("statusRequest")) {
            model.addAttribute("statusRequest", new AdminUserStatusRequest());
        }
    }

    private List<AccountStatusAction> allowedStatusActions(AccountStatus status) {
        return switch (status) {
            case ACTIVE -> List.of(AccountStatusAction.LOCK, AccountStatusAction.DEACTIVATE);
            case LOCKED -> List.of(AccountStatusAction.UNLOCK);
            case INACTIVE -> List.of(AccountStatusAction.REACTIVATE);
            case WITHDRAWN -> List.of();
        };
    }
}
