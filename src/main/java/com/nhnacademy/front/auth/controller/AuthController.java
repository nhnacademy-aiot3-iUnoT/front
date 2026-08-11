package com.nhnacademy.front.auth.controller;

import com.nhnacademy.front.auth.client.AuthApiClient;
import com.nhnacademy.front.auth.dto.request.CheckEmailRequest;
import com.nhnacademy.front.auth.dto.request.LoginRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordFormRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.request.SignupRequest;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.auth.dto.response.SignupResponse;
import com.nhnacademy.front.auth.validator.PasswordResetFormValidator;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.client.InvitationApiClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthApiClient authApiClient;
    private final InvitationApiClient invitationApiClient;
    private final PasswordResetFormValidator passwordResetFormValidator;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPost(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        log.info("Login Email: {}", request.email());

        LoginResponse loginResponse = authApiClient.login(request);

        ResponseCookie cookie = ResponseCookie.from("access_token", loginResponse.accessToken())
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signup(@RequestParam(required = false) String token, Model model) {
        if(token == null || token.isBlank()) {
            return "redirect:/login?error=invite";
        }

        invitationApiClient.verifyToken(token);

        model.addAttribute(
                "signupRequest",
                new SignupRequest(token, "", "", "")
        );

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPost(
            @Valid @ModelAttribute("signupRequest") SignupRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        log.info("Signup Token: {}", request.inviteToken());
        log.info("Signup Email: {}", request.email());
        log.info("Signup Name: {}", request.name());

        authApiClient.signup(request);

        return "redirect:/login";
    }

    @PostMapping("/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(
            @Valid @RequestBody CheckEmailRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return validationError(bindingResult);
        }

        log.info("Check Email: {}", request.email());

        return ResponseEntity.ok(authApiClient.checkEmail(request));
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("resetPasswordTokenRequest", new ResetPasswordTokenRequest(""));
        return "auth/forgot_password";
    }

    @PostMapping("/pwd")
    public String passwordResetToken(
            @Valid ResetPasswordTokenRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/forgot-password";
        }

        authApiClient.passwordResetToken(request);

        return "redirect:/login";
    }

    @GetMapping("/pwd/{token}")
    public String resetPassword(
            @PathVariable("token") String token,
            Model model
    ) {
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordForm", new ResetPasswordFormRequest());
        return "auth/reset_password";
    }

    @PostMapping("/pwd/{token}")
    public String resetPassword(
            @PathVariable("token") String token,
            @Valid @ModelAttribute("resetPasswordForm") ResetPasswordFormRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        passwordResetFormValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/reset_password";
        }

        authApiClient.resetPassword(
                new ResetPasswordRequest(request.newPassword()),
                token
        );
        return "redirect:/login";
    }

    private ResponseEntity<ApiResponse<Void>> validationError(BindingResult bindingResult) {
        String message = bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값을 확인해주세요.");

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }
}
