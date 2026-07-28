package com.nhnacademy.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {
    // [임시] 담당자가 직접 url 변경하기
    @GetMapping("/login")
    public String test() {
       return "auth/login";
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
    public String info() { return "account/account_info";}

    @GetMapping("/mypage/change-password")
    public String password() { return "account/change_password"; }

    @GetMapping("/withdraw")
    public String withdraw() { return "account/withdraw"; }
}
