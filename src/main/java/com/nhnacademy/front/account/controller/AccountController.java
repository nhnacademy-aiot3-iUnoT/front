package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.account.dto.request.LoginRequest;
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

    private final GatewayClient gatewayClient;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequest("", ""));
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginReq(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        log.info("Login Email: {}", request.email());
        log.info("Login Password: {}", request.password().hashCode());
//        ApiResponse<?> response = gatewayClient.post("/login", request, LoginResponse.class);
//        if (!response.success()) {
//            return "auth/login";
//        }

        return "redirect:/";
    }


    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
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
