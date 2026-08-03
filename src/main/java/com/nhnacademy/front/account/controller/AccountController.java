package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.*;
import com.nhnacademy.front.account.dto.response.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

         return "redirect:/";
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
    public CheckEmailResponse checkEmail(
            @Valid @RequestBody CheckEmailRequest request
    ) {
        log.info("Check Email: {}", request.email());

        return accountApiClient.checkEmail(request);
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
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
            RedirectAttributes redirectAttributes
    ) {

        accountApiClient.changeName(request);
        redirectAttributes.addFlashAttribute("successMessage", "회원정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/change-password")
    public String password() {
        return "account/change_password";
    }

    @PutMapping("/mypage/change-password")
    public String changePassword(
            @Valid @ModelAttribute UpdateAccountPasswordRequest request
    ) {

        accountApiClient.changePassword(request);
        return "redirect:/mypage";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "account/withdraw";
    }

    @DeleteMapping("withdraw")
    public WithdrawAccountResponse deleteAccount(
            @Valid @ModelAttribute WithdrawAccountRequest request
    ) {
        return accountApiClient.withdraw(request);
    }
}
