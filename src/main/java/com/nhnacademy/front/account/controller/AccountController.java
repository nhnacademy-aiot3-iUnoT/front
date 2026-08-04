package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.*;
import com.nhnacademy.front.account.dto.response.*;
import com.nhnacademy.front.global.dto.ApiResponse;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountApiClient accountApiClient;

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

        LoginResponse loginResponse = accountApiClient.login(request);

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
    public String logout(
            HttpServletResponse response
    ) {
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
    public String signup(
            Model model
    ) {
        model.addAttribute(
                "signupRequest",
                new SignupRequest("inviteToken", "", "", "")
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

        SignupResponse response = accountApiClient.signup(request);

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

        return ResponseEntity.ok(accountApiClient.checkEmail(request));
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("resetPasswordTokenRequest", new ResetPasswordTokenRequest(""));
        return "auth/forgot_password";
    }

    @GetMapping("/mypage")
    public String info(
            Model model
    ) {

        AccountInfoResponse response = accountApiClient.getAccountInfo();
        model.addAttribute("accountInfoResponse", response);

        return "account/account_info";
    }

    @PutMapping("/mypage")
    public String changeName(
            @Valid @ModelAttribute UpdateAccountNameRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/mypage";
        }

        accountApiClient.changeName(request);
        redirectAttributes.addFlashAttribute("successMessage", "회원정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/change-password")
    public String password(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordFormRequest());
        return "account/change_password";
    }

    @PutMapping("/mypage/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordFormRequest request,
            BindingResult bindingResult
    ) {
        if (hasText(request.currentPassword())
                && request.currentPassword().equals(request.newPassword())) {
            bindingResult.rejectValue(
                    "newPassword",
                    "sameAsCurrentPassword",
                    "새 비밀번호는 기존 비밀번호와 달라야 합니다."
            );
        }

        if (hasText(request.newPassword())
                && hasText(request.confirmPassword())
                && !request.newPassword().equals(request.confirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "새 비밀번호가 일치하지 않습니다."
            );
        }

        if (bindingResult.hasErrors()) {
            return "account/change_password";
        }

        accountApiClient.changePassword(new UpdateAccountPasswordRequest(request.newPassword()));
        return "redirect:/mypage";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "account/withdraw";
    }

    @DeleteMapping("/withdraw")
    @ResponseBody
    public String deleteAccount(
            @Valid @RequestBody WithdrawAccountRequest request,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/withdraw";
        }

        accountApiClient.withdraw(request);

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

    @PostMapping("/pwd")
    public String passwordResetToken(
            @Valid ResetPasswordTokenRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/forgot-password";
        }

        accountApiClient.passwordResetToken(request);

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
        if (hasText(request.newPassword())
                && hasText(request.confirmPassword())
                && !request.newPassword().equals(request.confirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "새 비밀번호가 일치하지 않습니다."
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/reset_password";
        }

        accountApiClient.resetPassword(
                new UpdateAccountPasswordRequest(request.newPassword()),
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
