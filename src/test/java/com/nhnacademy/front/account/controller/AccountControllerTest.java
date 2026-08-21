package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import jakarta.servlet.http.HttpServletResponse;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberRoleResponse;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(PasswordFormValidator.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountApiClient accountApiClient;

    @MockitoBean
    private AccessTokenCookieManager cookieManager;

    @MockitoBean
    private OrganizationMemberApiClient organizationMemberApiClient;

    @MockitoBean
    private DepartmentApiClient departmentApiClient;


    @Test
    void info() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 5, 12, 0);
        AccountInfoResponse response =
                new AccountInfoResponse("test@test.com", "test", AccountRole.USER, createdAt);

        given(accountApiClient.getAccountInfo())
                .willReturn(response);
        given(organizationMemberApiClient.getRole())
                .willReturn(new OrganizationMemberRoleResponse(OrganizationRole.ORG_MEMBER));
        given(departmentApiClient.getMyDepartments())
                .willReturn(List.of());

        mockMvc.perform(get("/mypage"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/account-info"))
                .andExpect(model().attribute("accountInfoResponse", response))
                .andExpect(model().attribute(
                        "nameRequest", new UpdateAccountNameRequest(response.name())
                ));

        then(accountApiClient).should().getAccountInfo();
    }

    @Test
    void changeName() throws Exception {
        UpdateAccountNameRequest request = new UpdateAccountNameRequest("test");

        mockMvc.perform(put("/mypage")
                        .param("name", request.name())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/mypage"))
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attribute("successMessage", "회원정보가 수정되었습니다."));

        then(accountApiClient).should().changeName(request);
    }

    @Test
    void changeNameError() throws Exception {
        AccountInfoResponse response = new AccountInfoResponse(
                "test@test.com", "기존 이름", AccountRole.USER,
                LocalDateTime.of(2026, 8, 5, 12, 0)
        );
        given(accountApiClient.getAccountInfo()).willReturn(response);

        MvcResult result = mockMvc.perform(put("/mypage").param("name", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("account/account-info"))
                .andExpect(model().attribute("accountInfoResponse", response))
                .andExpect(model().attributeHasFieldErrors("nameRequest", "name"))
                .andReturn();

        assertThat(fieldErrorMessage(result, "nameRequest", "name"))
                .isEqualTo("이름을 입력해주세요.");

        then(accountApiClient).should().getAccountInfo();
        then(accountApiClient).should(never()).changeName(any());
    }

    private static String fieldErrorMessage(MvcResult result, String objectName, String field) {
        BindingResult bindingResult = (BindingResult) Objects.requireNonNull(result.getModelAndView())
                .getModel()
                .get(BindingResult.MODEL_KEY_PREFIX + objectName);

        return Objects.requireNonNull(bindingResult.getFieldError(field)).getDefaultMessage();
    }

    @Test
    void passwordForm() throws Exception {

        mockMvc.perform(get("/mypage/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/change-password"))
                .andExpect(model().attribute("changePasswordForm", new ChangePasswordFormRequest()));
    }

    @Test
    void changePassword() throws Exception {
        ChangePasswordFormRequest formRequest =
                new ChangePasswordFormRequest(
                        "testtest",
                        "12341234",
                        "12341234"
                );

        ChangeOwnPasswordRequest request = new ChangeOwnPasswordRequest(
                formRequest.currentPassword(),
                formRequest.newPassword()
        );

        mockMvc.perform(put("/mypage/change-password")
                        .param("currentPassword", formRequest.currentPassword())
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/mypage"))
                .andExpect(redirectedUrl("/mypage"));

        then(accountApiClient).should().changePassword(request);
    }

    @Test
    void changePasswordWithPasswordValidateWithSamePassword() throws Exception {
        ChangePasswordFormRequest formRequest =
                new ChangePasswordFormRequest(
                        "12341234",
                        "12341234",
                        "12341234"
                );

        mockMvc.perform(put("/mypage/change-password")
                        .param("currentPassword", formRequest.currentPassword())
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/change-password"))
                .andExpect(model().attributeHasFieldErrors(
                        "changePasswordForm",
                        "newPassword"
                ));

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void changePasswordWithPasswordValidateWithDifferentConfirmPassword() throws Exception {
        ChangePasswordFormRequest formRequest =
                    new ChangePasswordFormRequest(
                            "oldpass1",
                            "newpass1",
                            "newpass2"
                    );

        mockMvc.perform(put("/mypage/change-password")
                        .param("currentPassword", formRequest.currentPassword())
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/change-password"))
                .andExpect(model().attributeHasFieldErrors(
                        "changePasswordForm",
                        "confirmPassword"
                ));

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void changePasswordWithBindingError() throws Exception {
        ChangePasswordFormRequest formRequest =
                new ChangePasswordFormRequest(
                        "asd",
                        "12341234",
                        "12341234"
                );

        mockMvc.perform(put("/mypage/change-password")
                        .param("currentPassword", formRequest.currentPassword())
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/change-password"));

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void withdrawPageIsRendered() throws Exception {
        mockMvc.perform(get("/withdraw"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/withdraw"));
    }

    @Test
    void deleteAccountWithdrawsAccountAndClearsCookie() throws Exception {
        WithdrawAccountRequest request = new WithdrawAccountRequest("password");

        mockMvc.perform(delete("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"password"}
                                """))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"));

        then(accountApiClient).should().withdraw(request);
        then(cookieManager).should().delete(any(HttpServletResponse.class));
    }

    @Test
    void deleteAccountWithInvalidPasswordDoesNotCallDependencies() throws Exception {
        mockMvc.perform(delete("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"12345"}
                                """))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/withdraw"))
                .andExpect(redirectedUrl("/withdraw"));

        then(accountApiClient).shouldHaveNoInteractions();
        then(cookieManager).shouldHaveNoInteractions();
    }
}
