package com.nhnacademy.front.global.controller;

import com.nhnacademy.front.global.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.time.Instant;

@ControllerAdvice
@RequiredArgsConstructor
public class AccessTokenModelAdvice {

    private final JwtProperties jwtProperties;

    @ModelAttribute
    public void addAccessTokenExpiration(Principal principal, Model model) {
        model.addAttribute(
                "accessTokenDurationSeconds",
                jwtProperties.getAccessTokenTtl().toSeconds()
        );

        if (!(principal instanceof JwtAuthenticationToken authentication)) {
            return;
        }

        Instant expiresAt = authentication.getToken().getExpiresAt();
        if (expiresAt != null) {
            model.addAttribute("accessTokenExpiresAt", expiresAt.toEpochMilli());
        }
    }
}
