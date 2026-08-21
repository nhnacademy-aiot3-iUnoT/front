package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(PasswordFormValidator.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private AccountApiClient accountApiClient;

    private PasswordFormValidator passwordFormValidator;


    @Test
    void info() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 5, 12, 0);
        AccountInfoResponse response =
                new AccountInfoResponse("test@test.com", "test", AccountRole.USER, createdAt);

        given(accountApiClient.getAccountInfo())
                .willReturn(response);

        mockMvc.perform(get("/mypage"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/account-info"))
                .andExpect(model().attribute("accountInfoResponse", response));

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
        UpdateAccountNameRequest request = new UpdateAccountNameRequest("");

        mockMvc.perform(put("/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/mypage"))
                .andExpect(redirectedUrl("/mypage"));

        then(accountApiClient).shouldHaveNoInteractions();
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
    void withdraw() {
    }

    @Test
    void deleteAccount() {
    }
}
