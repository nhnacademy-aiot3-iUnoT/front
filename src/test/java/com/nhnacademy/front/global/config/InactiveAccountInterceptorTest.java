package com.nhnacademy.front.global.config;

import com.nhnacademy.front.account.dto.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InactiveAccountInterceptorTest {

    private final InactiveAccountInterceptor interceptor = new InactiveAccountInterceptor();

    @Test
    void unauthenticatedRequestCanContinue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void activeAccountCanContinue() throws Exception {
        MockHttpServletRequest request = authenticatedRequest("/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(200, response.getStatus());
    }

    @Test
    void inactiveAccountIsRedirectedToReactivationPage() throws Exception {
        MockHttpServletRequest request = authenticatedRequest("/front/mypage", AccountStatus.INACTIVE);
        request.setContextPath("/front");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(302, response.getStatus());
        assertEquals("/front/reactivation", response.getRedirectedUrl());
    }

    private MockHttpServletRequest authenticatedRequest(String path) {
        return authenticatedRequest(path, AccountStatus.ACTIVE);
    }

    private MockHttpServletRequest authenticatedRequest(String path, AccountStatus status) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim(SecurityConfig.ACCOUNT_STATUS_CLAIM, status.name())
                .build();
        request.setUserPrincipal(new JwtAuthenticationToken(jwt, List.of()));
        return request;
    }
}
