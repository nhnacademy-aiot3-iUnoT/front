package com.nhnacademy.front.global.config;

import com.nhnacademy.front.account.dto.AccountStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class InactiveAccountInterceptor implements HandlerInterceptor {

    static final String REACTIVATION_PATH = "/reactivation";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        if (!(request.getUserPrincipal() instanceof JwtAuthenticationToken authentication)
                || !authentication.isAuthenticated()) {
            return true;
        }

        String accountStatus = authentication.getToken()
                .getClaimAsString(SecurityConfig.ACCOUNT_STATUS_CLAIM);

        if (!AccountStatus.INACTIVE.name().equals(accountStatus)) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + REACTIVATION_PATH);
        return false;
    }
}
