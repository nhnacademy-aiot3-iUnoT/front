package com.nhnacademy.front.global.security;

import com.nhnacademy.front.global.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
public class AccessTokenCookieManager {

    public static final String COOKIE_NAME = "access_token";

    private final boolean secure;
    private final Duration accessTokenTtl;

    public AccessTokenCookieManager(
            @Value("${cookie.secure:false}") boolean secure,
            JwtProperties jwtProperties
    ) {
        this.secure = secure;
        this.accessTokenTtl = jwtProperties.getAccessTokenTtl();
    }

    public void add(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = baseCookie(accessToken)
                .maxAge(accessTokenTtl)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void delete(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie("")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String resolve(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        return cookie != null && StringUtils.hasText(cookie.getValue())
                ? cookie.getValue()
                : null;
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/");
    }
}
