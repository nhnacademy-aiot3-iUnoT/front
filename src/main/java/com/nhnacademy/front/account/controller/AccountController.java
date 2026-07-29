package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.CheckEmailRequest;
import com.nhnacademy.front.account.dto.request.SignupRequest;
import com.nhnacademy.front.account.dto.request.LoginRequest;
import com.nhnacademy.front.account.dto.response.CheckEmailResponse;
import com.nhnacademy.front.account.dto.response.SignupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountApiClient accountApiClient;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequest("", ""));
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPost(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        log.info("Login Email: {}", request.email());
        log.info("Login Password: {}", request.password().hashCode());
        // LoginResponse response = accountApiClient.login(request);

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
        log.info("Signup Password: {}", request.password().hashCode());

        // SignupResponse response = accountApiClient.signup(request);

        return "redirect:/";
    }

    @PostMapping("/check-email")
    @ResponseBody
    public CheckEmailResponse checkEmail(
            @Valid @RequestBody CheckEmailRequest request
    ) {
        log.info("Check Email: {}", request.email());

        CheckEmailResponse response = request.email().contains("test") ? new CheckEmailResponse(true) : new CheckEmailResponse(false);
        // CheckEmailResponse response = accountApiClient.checkEmail(request);

        return response;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/mypage")
    public String info() {
        return "account/account_info";
    }

    @GetMapping("/mypage/change-password")
    public String password() {
        return "account/change_password";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "account/withdraw";
    }
}
