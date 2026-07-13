package com.nhnacademy.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {
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

    @GetMapping("/forgot-id")
    public String forgotId() {
        return "auth/forgot-id";
    }
}
