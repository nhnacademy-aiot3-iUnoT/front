package com.nhnacademy.front.global.config;

import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final BearerTokenResolver bearerTokenResolver =
            new SecurityConfig().bearerTokenResolver();

    @Test
    void refreshRequestDoesNotUseExpiredAccessTokenCookie() {
        MockHttpServletRequest request = request("POST", "/refresh");

        assertThat(bearerTokenResolver.resolve(request)).isNull();
    }

    @Test
    void protectedRequestUsesAccessTokenCookie() {
        MockHttpServletRequest request = request("GET", "/mypage");

        assertThat(bearerTokenResolver.resolve(request)).isEqualTo("access-token");
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setCookies(new Cookie(
                AccessTokenCookieManager.COOKIE_NAME,
                "access-token"
        ));
        return request;
    }
}
