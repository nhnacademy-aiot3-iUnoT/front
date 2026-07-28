package com.nhnacademy.front.controller;

import com.nhnacademy.front.client.GatewayClient;
import com.nhnacademy.front.dto.ApiResponse;
import com.nhnacademy.front.dto.auth.LoginRequest;
import com.nhnacademy.front.dto.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final GatewayClient gatewayClient;

    // [임시] 담당자가 직접 url 변경하기
    @GetMapping("/login")
    public String login() {
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
