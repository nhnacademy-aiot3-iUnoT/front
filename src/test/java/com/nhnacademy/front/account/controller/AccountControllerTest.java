package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.AccountStatus;
import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.ReactivationConfirmRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import com.nhnacademy.front.global.config.InactiveAccountInterceptor;
import com.nhnacademy.front.global.config.SecurityConfig;
import com.nhnacademy.front.global.config.WebMvcConfig;
import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        PasswordFormValidator.class,
        WebMvcConfig.class,
        InactiveAccountInterceptor.class
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountApiClient accountApiClient;

    @MockitoBean
    private AccessTokenCookieManager cookieManager;

    @Test
    void info() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 5, 12, 0);
        AccountInfoResponse response =
                new AccountInfoResponse(
                        "test@test.com",
                        "test",
                        AccountRole.USER,
                        AccountStatus.ACTIVE,
                        createdAt
                );

        given(accountApiClient.getAccountInfo())
                .willReturn(response);

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
    void inactiveAccountIsStillBlockedFromOtherAccountPages() throws Exception {
        mockMvc.perform(get("/mypage")
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reactivation"));

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void reactivationWithoutTokenShowsVerificationRequest() throws Exception {
        ReactivationConfirmRequest request = new ReactivationConfirmRequest("");

        MvcResult result = mockMvc.perform(get("/reactivation")
                        .cookie(new Cookie("access_token", "inactive-token"))
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().isOk())
                .andExpect(view().name("account/reactivation"))
                .andExpect(model().attribute("reactivationConfirmRequest", request))
                .andReturn();

        Document document = Jsoup.parse(result.getResponse().getContentAsString());
        assertThat(document.select("form#reactivation-verification-form")).hasSize(1);
        assertThat(document.select("form#reactivation-confirm-form")).isEmpty();

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void reactivationWithTokenShowsExplicitConfirmationForm() throws Exception {
        String token = "a".repeat(64);
        ReactivationConfirmRequest request = new ReactivationConfirmRequest(token);

        MvcResult result = mockMvc.perform(get("/reactivation")
                        .param("token", token)
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().isOk())
                .andExpect(view().name("account/reactivation"))
                .andExpect(model().attribute("reactivationConfirmRequest", request))
                .andReturn();

        Document document = Jsoup.parse(result.getResponse().getContentAsString());
        assertThat(document.select("form#reactivation-confirm-form")).hasSize(1);
        assertThat(document.select(
                "form#reactivation-confirm-form input[type=hidden][name=token][value=" + token + "]"
        )).hasSize(1);
        assertThat(document.select("form#reactivation-confirm-form button[type=submit]")).hasSize(1);

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void verificationRequestShowsAcceptedMessage() throws Exception {
        mockMvc.perform(post("/reactivation/verification")
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reactivation"))
                .andExpect(redirectedUrl("/reactivation"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "인증 메일 발송 요청을 접수했습니다. 이메일을 확인해주세요."
                ));

        then(accountApiClient).should().requestReactivationVerification();
    }

    @Test
    void verificationRequestFailureReturnsToSamePageWithMessage() throws Exception {
        willThrow(new ApiException(ErrorCode.UNKNOWN, "메일 발송 요청에 실패했습니다."))
                .given(accountApiClient)
                .requestReactivationVerification();

        mockMvc.perform(post("/reactivation/verification")
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reactivation"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "메일 발송 요청에 실패했습니다."
                ));

        then(accountApiClient).should().requestReactivationVerification();
    }

    @Test
    void confirmReactivationDeletesAccessTokenAndRequiresLoginAgain() throws Exception {
        ReactivationConfirmRequest request = new ReactivationConfirmRequest("a".repeat(64));

        mockMvc.perform(post("/reactivation/confirm")
                        .param("token", request.token())
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?reactivated"))
                .andExpect(redirectedUrl("/login?reactivated"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "계정이 재활성화되었습니다. 다시 로그인해주세요."
                ));

        then(accountApiClient).should().confirmReactivation(request);
        then(cookieManager).should().delete(any(HttpServletResponse.class));
    }

    @Test
    void malformedReactivationTokenStaysOnSamePage() throws Exception {
        mockMvc.perform(post("/reactivation/confirm")
                        .param("token", "invalid-token")
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().isOk())
                .andExpect(view().name("account/reactivation"))
                .andExpect(model().attributeHasFieldErrors(
                        "reactivationConfirmRequest",
                        "token"
                ));

        then(accountApiClient).shouldHaveNoInteractions();
        then(cookieManager).shouldHaveNoInteractions();
    }

    @Test
    void expiredReactivationTokenShowsClearErrorAndKeepsCookie() throws Exception {
        ReactivationConfirmRequest request = new ReactivationConfirmRequest("a".repeat(64));
        given(accountApiClient.confirmReactivation(request))
                .willThrow(new ApiException(ErrorCode.A009, "Invalid verification token"));

        mockMvc.perform(post("/reactivation/confirm")
                        .param("token", request.token())
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().isOk())
                .andExpect(view().name("account/reactivation"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "인증 링크가 유효하지 않거나 만료되었습니다. 인증 메일을 다시 요청해주세요."
                ));

        then(accountApiClient).should().confirmReactivation(request);
        then(cookieManager).shouldHaveNoInteractions();
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
                AccountStatus.ACTIVE,
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

    private JwtAuthenticationToken authentication(AccountStatus status) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim(SecurityConfig.ACCOUNT_STATUS_CLAIM, status.name())
                .build();
        return new JwtAuthenticationToken(jwt, List.of());
    }
}
