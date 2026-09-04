package com.nhnacademy.front.global.security;

import com.nhnacademy.front.global.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieAuthenticationEntryPointTest {

    private AccessTokenCookieManager cookieManager;
    private CookieAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenTtl(Duration.ofMinutes(30));

        cookieManager = new AccessTokenCookieManager(false, jwtProperties);
        entryPoint = new CookieAuthenticationEntryPoint(cookieManager);
    }

    @Test
    void invalidAccessTokenCookieIsDeletedAndRedirectedToLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/front");
        request.setCookies(new Cookie(AccessTokenCookieManager.COOKIE_NAME, "invalid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("invalid token"));

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/front/login");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("access_token=")
                .contains("Path=/")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void unauthenticatedRequestWithoutCookieRedirectsWithoutSetCookieHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("authentication required"));

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void accessTokenCookieUsesConfiguredTtl() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieManager.add(response, "access-token");

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("access_token=access-token")
                .contains("Max-Age=1800")
                .contains("Path=/")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }
}
