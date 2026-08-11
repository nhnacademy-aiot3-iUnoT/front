package com.nhnacademy.front.auth.controller;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(PasswordResetFormValidator.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthApiClient authApiClient;

    @Test
    void login() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("loginRequest", new LoginRequest()));
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
                .andExpect(view().name("redirect:/"))
                .andExpect(redirectedUrl("/"))
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

    @Test
    void signup() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attribute(
                        "signupRequest",
                        new SignupRequest("inviteToken", "", "", "")));
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
                .andExpect(view().name("redirect:/organizations/me/setup"))
                .andExpect(redirectedUrl("/organizations/me/setup"));

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
                .andExpect(view().name("auth/forgot_password"))
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
                .andExpect(view().name("auth/reset_password"))
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
                .andExpect(view().name("auth/reset_password"))
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
                .andExpect(view().name("auth/reset_password"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "resetPasswordForm", "confirmPassword", "passwordMismatch"))
                .andExpect(model().attribute("token", token));

        then(authApiClient).shouldHaveNoInteractions();
    }
}
