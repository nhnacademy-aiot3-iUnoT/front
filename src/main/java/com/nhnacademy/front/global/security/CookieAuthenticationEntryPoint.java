package com.nhnacademy.front.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CookieAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AccessTokenCookieManager cookieManager;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        if (WebUtils.getCookie(request, AccessTokenCookieManager.COOKIE_NAME) != null) {
            cookieManager.delete(response);
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
