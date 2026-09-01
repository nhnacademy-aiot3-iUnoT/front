package com.nhnacademy.front.global.security;

import com.nhnacademy.front.global.config.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieManagerTest {

    private RefreshTokenCookieManager cookieManager;

    @BeforeEach
    void setUp() {
        RefreshTokenProperties properties = new RefreshTokenProperties();
        properties.setTtl(Duration.ofDays(14));
        cookieManager = new RefreshTokenCookieManager(false, properties);
    }

    @Test
    void addUsesProtectedFourteenDayRootCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieManager.add(response, "refresh-token");

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=refresh-token")
                .contains("Max-Age=1209600")
                .contains("Path=/")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void resolveReturnsOnlyNonBlankRefreshToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(RefreshTokenCookieManager.COOKIE_NAME, "refresh-token"));

        assertThat(cookieManager.resolve(request)).isEqualTo("refresh-token");
    }

    @Test
    void deleteExpiresRefreshTokenCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieManager.delete(response);

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=")
                .contains("Max-Age=0")
                .contains("Path=/");
    }
}
