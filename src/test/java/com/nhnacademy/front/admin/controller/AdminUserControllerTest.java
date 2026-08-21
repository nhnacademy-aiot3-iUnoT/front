package com.nhnacademy.front.admin.controller;

import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.admin.client.AdminApiClient;
import com.nhnacademy.front.admin.dto.AccountStatus;
import com.nhnacademy.front.admin.dto.AccountStatusAction;
import com.nhnacademy.front.admin.dto.request.AdminResetPasswordRequest;
import com.nhnacademy.front.admin.dto.request.AdminUserCreateRequest;
import com.nhnacademy.front.admin.dto.request.AdminUserStatusRequest;
import com.nhnacademy.front.admin.dto.response.AdminUserResponse;
import com.nhnacademy.front.global.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminApiClient adminApiClient;

    @Test
    void getUserList() throws Exception {
        AdminUserResponse user = user(
                "테스트", "test@example.com", AccountRole.USER, AccountStatus.ACTIVE, 1
        );
        given(adminApiClient.getUsers()).willReturn(List.of(user));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-list"))
                .andExpect(model().attribute(
                        "users",
                        new PageResponse<>(List.of(user), 0, 10, 1, 1, true)
                ))
                .andExpect(model().attribute("totalUserCount", 1))
                .andExpect(model().attribute("activeCount", 1L))
                .andExpect(model().attribute("adminCount", 0L));

        then(adminApiClient).should().getUsers();
    }

    @Test
    void getUserListAppliesSearchCategoryAndSort() throws Exception {
        AdminUserResponse zeta = user(
                "Zeta", "zeta@example.com", AccountRole.USER, AccountStatus.ACTIVE, 1
        );
        AdminUserResponse alpha = user(
                "Alpha", "alpha@example.com", AccountRole.USER, AccountStatus.ACTIVE, 2
        );
        AdminUserResponse admin = user(
                "Admin", "admin@example.com", AccountRole.ADMIN, AccountStatus.ACTIVE, 3
        );
        AdminUserResponse locked = user(
                "Locked", "locked@example.com", AccountRole.USER, AccountStatus.LOCKED, 4
        );
        given(adminApiClient.getUsers()).willReturn(List.of(zeta, alpha, admin, locked));

        mockMvc.perform(get("/admin/users")
                        .param("keyword", "example")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("sort", "NAME_ASC"))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "users",
                        new PageResponse<>(List.of(alpha, zeta), 0, 10, 2, 1, true)
                ));
    }

    @Test
    void getUserListAppliesPagination() throws Exception {
        List<AdminUserResponse> users = new ArrayList<>();
        for (int index = 0; index < 11; index++) {
            users.add(user(
                    "User " + index,
                    "user" + index + "@example.com",
                    AccountRole.USER,
                    AccountStatus.ACTIVE,
                    index
            ));
        }
        given(adminApiClient.getUsers()).willReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "CREATED_ASC"))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "users",
                        new PageResponse<>(List.of(users.get(10)), 1, 10, 11, 2, true)
                ));
    }

    @Test
    void getCreateAdminForm() throws Exception {
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-create"))
                .andExpect(model().attributeExists("createRequest"));
    }

    @Test
    void postCreateAdmin() throws Exception {
        AdminUserCreateRequest request = new AdminUserCreateRequest(
                "새 관리자", "new-admin@example.com", "admin1234"
        );

        mockMvc.perform(post("/admin/users")
                        .param("name", request.name())
                        .param("email", request.email())
                        .param("password", request.password()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("successMessage", "관리자 계정이 생성되었습니다."));

        then(adminApiClient).should().createAdmin(request);
    }

    @Test
    void postCreateAdminReturnsFormWhenInvalid() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .param("name", "")
                        .param("email", "invalid")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-create"))
                .andExpect(model().attributeHasFieldErrors(
                        "createRequest", "name", "email", "password"
                ));

        then(adminApiClient).should(never()).createAdmin(any());
    }

    @Test
    void getUserDetail() throws Exception {
        AdminUserResponse user = user(
                "관리자", "admin@example.com", AccountRole.ADMIN, AccountStatus.ACTIVE, 1
        );
        given(adminApiClient.getUser(user.uuid())).willReturn(user);

        mockMvc.perform(get("/admin/users/{uuid}", user.uuid()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("statusActions", contains(
                        AccountStatusAction.LOCK,
                        AccountStatusAction.DEACTIVATE
                )))
                .andExpect(model().attributeExists(
                        "nameRequest", "passwordRequest", "statusRequest"
                ));
    }

    @Test
    void putUpdateUserName() throws Exception {
        UUID uuid = UUID.randomUUID();
        UpdateAccountNameRequest request = new UpdateAccountNameRequest("변경된 이름");

        mockMvc.perform(put("/admin/users/{uuid}/name", uuid)
                        .param("name", request.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + uuid))
                .andExpect(flash().attribute("successMessage", "회원 이름을 수정했습니다."));

        then(adminApiClient).should().updateName(uuid, request);
    }

    @Test
    void putUpdateUserNameReturnsFormWhenInvalid() throws Exception {
        AdminUserResponse user = user(
                "관리자", "admin@example.com", AccountRole.ADMIN, AccountStatus.ACTIVE, 1
        );
        given(adminApiClient.getUser(user.uuid())).willReturn(user);

        mockMvc.perform(put("/admin/users/{uuid}/name", user.uuid())
                        .param("name", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attributeHasFieldErrors("nameRequest", "name"));

        then(adminApiClient).should(never()).updateName(any(), any());
    }

    @Test
    void putUpdateUserPassword() throws Exception {
        UUID uuid = UUID.randomUUID();
        AdminResetPasswordRequest request = new AdminResetPasswordRequest("changed1234");

        mockMvc.perform(put("/admin/users/{uuid}/password", uuid)
                        .param("password", request.password()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + uuid))
                .andExpect(flash().attribute("successMessage", "회원 비밀번호를 재설정했습니다."));

        then(adminApiClient).should().updatePassword(uuid, request);
    }

    @Test
    void putUpdateUserPasswordReturnsFormWhenInvalid() throws Exception {
        AdminUserResponse user = user(
                "관리자", "admin@example.com", AccountRole.ADMIN, AccountStatus.ACTIVE, 1
        );
        given(adminApiClient.getUser(user.uuid())).willReturn(user);

        mockMvc.perform(put("/admin/users/{uuid}/password", user.uuid())
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attributeHasFieldErrors("passwordRequest", "password"));

        then(adminApiClient).should(never()).updatePassword(any(), any());
    }

    @Test
    void putChangeUserStatus() throws Exception {
        UUID uuid = UUID.randomUUID();
        AdminUserStatusRequest request = new AdminUserStatusRequest(
                AccountStatusAction.LOCK, "보안 점검"
        );

        mockMvc.perform(put("/admin/users/{uuid}/status", uuid)
                        .param("action", request.action().name())
                        .param("reason", request.reason()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + uuid))
                .andExpect(flash().attribute("successMessage", "회원 상태를 변경했습니다."));

        then(adminApiClient).should().changeStatus(uuid, request);
    }

    @Test
    void putChangeUserStatusReturnsFormWhenInvalid() throws Exception {
        AdminUserResponse user = user(
                "관리자", "admin@example.com", AccountRole.ADMIN, AccountStatus.ACTIVE, 1
        );
        given(adminApiClient.getUser(user.uuid())).willReturn(user);

        mockMvc.perform(put("/admin/users/{uuid}/status", user.uuid())
                        .param("action", "")
                        .param("reason", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attributeHasFieldErrors(
                        "statusRequest", "action", "reason"
                ));

        then(adminApiClient).should(never()).changeStatus(any(), any());
    }

    private AdminUserResponse user(
            String name,
            String email,
            AccountRole role,
            AccountStatus status,
            int createdMinute
    ) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 12, 0)
                .plusMinutes(createdMinute);
        return new AdminUserResponse(
                UUID.randomUUID(),
                name,
                email,
                role,
                status,
                createdAt,
                createdAt,
                null
        );
    }
}
