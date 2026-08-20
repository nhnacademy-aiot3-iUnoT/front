package com.nhnacademy.front.auth.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.AccountStatus;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.auth.client.AuthApiClient;
import com.nhnacademy.front.auth.dto.request.CheckEmailRequest;
import com.nhnacademy.front.auth.dto.request.LoginRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordFormRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.request.SignupRequest;
import com.nhnacademy.front.auth.dto.response.CheckEmailResponse;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.auth.dto.response.SignupResponse;
import com.nhnacademy.front.auth.validator.PasswordResetFormValidator;
import com.nhnacademy.front.organization.client.InvitationApiClient;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PasswordResetFormValidator.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthApiClient authApiClient;

    @MockitoBean
    AccountApiClient accountApiClient;

    @MockitoBean
    OrganizationApiClient organizationApiClient;

    @MockitoBean
    InvitationApiClient invitationApiClient;

    @Test
    void login() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("loginRequest", new LoginRequest()));
    }

    @Test
    void reactivatedAccountIsAskedToLoginAgain() throws Exception {
        mockMvc.perform(get("/login").param("reactivated", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("계정이 재활성화되었습니다. 다시 로그인해주세요.")));
    }

    @Test
    void jwks() throws Exception {
        Map<String, Object> jwks = Map.of(
                "keys",
                List.of(Map.of(
                        "kty", "RSA",
                        "kid", "test-key",
                        "n", "modulus",
                        "e", "AQAB"
                ))
        );
        given(authApiClient.jwks()).willReturn(jwks);

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").value("test-key"))
                .andExpect(jsonPath("$.keys[0].n").value("modulus"))
                .andExpect(jsonPath("$.keys[0].e").value("AQAB"));

        then(authApiClient).should().jwks();
    }

    @Test
    void loginPost() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        LoginResponse response = new LoginResponse("access-token");

        given(authApiClient.login(request)).willReturn(response);

        mockMvc.perform(post("/login")
                        .param("email", request.email())
                        .param("password", request.password()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login/success"))
                .andExpect(redirectedUrl("/login/success"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("access_token=access-token")))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("SameSite=Lax")));

        then(authApiClient).should().login(request);
    }

    @Test
    void adminIsRedirectedToAdminPageAfterLogin() throws Exception {
        AccountInfoResponse account = new AccountInfoResponse(
                "admin@example.com",
                "관리자",
                AccountRole.ADMIN,
                AccountStatus.ACTIVE,
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );
        given(accountApiClient.getAccountInfo()).willReturn(account);

        mockMvc.perform(get("/login/success"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"))
                .andExpect(redirectedUrl("/admin"));

        then(accountApiClient).should().getAccountInfo();
        then(organizationApiClient).shouldHaveNoInteractions();
    }

    @Test
    void inactiveAccountCannotOpenLoginPage() throws Exception {
        mockMvc.perform(get("/login")
                        .principal(authentication(AccountStatus.INACTIVE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reactivation"));

        then(accountApiClient).shouldHaveNoInteractions();
    }

    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("access_token=")))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Max-Age=0")));
    }

    private JwtAuthenticationToken authentication(AccountStatus status) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("account_status", status.name())
                .build();
        return new JwtAuthenticationToken(jwt, List.of());
    }

    @Test
    void signup() throws Exception {
        mockMvc.perform(get("/signup")
                        .param("token", "inviteToken"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attribute(
                        "signupRequest",
                        new SignupRequest("inviteToken", "", "", "")));

        then(invitationApiClient).should().verifyToken("inviteToken");
    }

    @Test
    void signupWithoutToken() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?error=invite"))
                .andExpect(redirectedUrl("/login?error=invite"));

        then(invitationApiClient).shouldHaveNoInteractions();
    }

    @Test
    void signupPostForOwner() throws Exception {
        SignupRequest request = new SignupRequest(
                "invite-token",
                "owner@test.com",
                "owner",
                "password"
        );

        given(authApiClient.signup(request)).willReturn(new SignupResponse(true));

        mockMvc.perform(post("/signup")
                        .param("inviteToken", request.inviteToken())
                        .param("email", request.email())
                        .param("name", request.name())
                        .param("password", request.password()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"));

        then(authApiClient).should().signup(request);
    }

    @Test
    void signupPostForMember() throws Exception {
        SignupRequest request = new SignupRequest(
                "invite-token",
                "member@test.com",
                "member",
                "password"
        );

        given(authApiClient.signup(request)).willReturn(new SignupResponse(false));

        mockMvc.perform(post("/signup")
                        .param("inviteToken", request.inviteToken())
                        .param("email", request.email())
                        .param("name", request.name())
                        .param("password", request.password()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"));

        then(authApiClient).should().signup(request);
    }

    @Test
    void signupPostWithValidationError() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("inviteToken", "invite-token")
                        .param("email", "invalid-email")
                        .param("name", "")
                        .param("password", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attributeHasFieldErrors(
                        "signupRequest",
                        "email",
                        "name",
                        "password"));

        then(authApiClient).shouldHaveNoInteractions();
    }

    @Test
    void checkEmail() throws Exception {
        CheckEmailRequest request = new CheckEmailRequest("test@test.com");
        CheckEmailResponse response = new CheckEmailResponse(true);

        given(authApiClient.checkEmail(request)).willReturn(response);

        mockMvc.perform(post("/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available").value(true));

        then(authApiClient).should().checkEmail(request);
    }

    @Test
    void checkEmailWithError() throws Exception {
        mockMvc.perform(post("/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());

        then(authApiClient).shouldHaveNoInteractions();
    }

    @Test
    void forgotPassword() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attribute("resetPasswordTokenRequest", new ResetPasswordTokenRequest("")));
    }

    @Test
    void passwordResetToken() throws Exception {

        ResetPasswordTokenRequest request = new ResetPasswordTokenRequest("test@test.com");

        mockMvc.perform(post("/pwd")
                    .param("email", request.email()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"));

        then(authApiClient).should().passwordResetToken(request);
    }


    @Test
    void passwordResetTokenWithError() throws Exception {

        ResetPasswordTokenRequest request = new ResetPasswordTokenRequest("12345");

        mockMvc.perform(post("/pwd")
                    .param("email", request.email()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(redirectedUrl("/forgot-password"));

        then(authApiClient).shouldHaveNoInteractions();
    }

    @Test
    void ResetPasswordForm() throws Exception {
        String token = "t".repeat(64);

        mockMvc.perform(get("/pwd/{token}", token))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attribute("resetPasswordForm", new ResetPasswordFormRequest()))
                .andExpect(model().attribute("token", token));

    }

    @Test
    void resetPassword() throws Exception {
        String token = "t".repeat(64);
        ResetPasswordFormRequest formRequest = new ResetPasswordFormRequest("newPwd", "newPwd");

        mockMvc.perform(post("/pwd/{token}", token)
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"));

        then(authApiClient).should()
                .resetPassword(new ResetPasswordRequest(formRequest.newPassword()), token);
    }


    @Test
    void resetPasswordWithBeanValidationError() throws Exception {
        String token = "t".repeat(64);
        ResetPasswordFormRequest formRequest = new ResetPasswordFormRequest("12345", "12345");

        mockMvc.perform(post("/pwd/{token}", token)
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "resetPasswordForm", "newPassword", "Size"))
                .andExpect(model().attribute("token", token));

        then(authApiClient).shouldHaveNoInteractions();
    }

    @Test
    void resetPasswordWithValidator() throws Exception {
        String token = "t".repeat(64);
        ResetPasswordFormRequest formRequest = new ResetPasswordFormRequest("newPwd", "newPed");

        mockMvc.perform(post("/pwd/{token}", token)
                        .param("newPassword", formRequest.newPassword())
                        .param("confirmPassword", formRequest.confirmPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "resetPasswordForm", "confirmPassword", "passwordMismatch"))
                .andExpect(model().attribute("token", token));

        then(authApiClient).shouldHaveNoInteractions();
    }
}
