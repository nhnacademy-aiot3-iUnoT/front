package com.nhnacademy.front.auth.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.admin.dto.OrganizationStatus;
import com.nhnacademy.front.auth.client.AuthApiClient;
import com.nhnacademy.front.auth.dto.request.CheckEmailRequest;
import com.nhnacademy.front.auth.dto.request.LoginRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordFormRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.request.SignupFormRequest;
import com.nhnacademy.front.auth.dto.request.SignupRequest;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.auth.service.AuthSessionService;
import com.nhnacademy.front.auth.validator.PasswordResetFormValidator;
import com.nhnacademy.front.auth.validator.SignupFormValidator;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.client.InvitationApiClient;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthApiClient authApiClient;
    private final AccountApiClient accountApiClient;
    private final OrganizationApiClient organizationApiClient;

    private final InvitationApiClient invitationApiClient;
    private final PasswordResetFormValidator passwordResetFormValidator;
    private final SignupFormValidator signupFormValidator;
    private final AuthSessionService authSessionService;

    @GetMapping("/login")
    public String login(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }

        model.addAttribute(new LoginRequest());
        return "auth/login";
    }

    @GetMapping("/.well-known/jwks.json")
    @ResponseBody
    public Map<String, Object> jwks() {
        return authApiClient.jwks();
    }

    // 임시
    @GetMapping("/login/success")
    public String loginSuccess() {
        AccountInfoResponse account = accountApiClient.getAccountInfo();

        if (account.accountRole() == AccountRole.ADMIN) {
            return "redirect:/admin";
        }

        OrgDetailResponse organization = organizationApiClient.getOrgInfo();

        if (organization.status() == OrganizationStatus.PENDING) {
            return "redirect:/organizations/me/setup";
        }

        return "redirect:/";
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

        authSessionService.establish(response, loginResponse);

        return "redirect:/login/success";
    }

    @PostMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authSessionService.revoke(request, response);

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
                new SignupFormRequest(token, "", "", "", "")
        );

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPost(
            @Valid @ModelAttribute("signupRequest") SignupFormRequest request,
            BindingResult bindingResult
    ) {
        signupFormValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        log.info("Signup Email: {}", request.email());
        log.info("Signup Name: {}", request.name());

        authApiClient.signup(new SignupRequest(
                request.inviteToken(),
                request.email(),
                request.name(),
                request.password()
        ));

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
        return "auth/forgot-password";
    }

    @PostMapping("/pwd")
    public String passwordResetToken(
            @Valid @ModelAttribute("resetPasswordTokenRequest") ResetPasswordTokenRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        authApiClient.passwordResetToken(request);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "비밀번호 재설정 메일 발송 요청을 접수했습니다."
        );

        return "redirect:/forgot-password";
    }

    @GetMapping("/pwd/{token}")
    public String resetPassword(
            @PathVariable("token") String token,
            Model model
    ) {
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordForm", new ResetPasswordFormRequest());
        return "auth/reset-password";
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
            return "auth/reset-password";
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
