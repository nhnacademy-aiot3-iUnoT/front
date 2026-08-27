package com.nhnacademy.front.global.security;

import com.nhnacademy.front.global.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenCookieManagerTest {

    @Test
    void deleteExpiresAccessTokenCookieAtRootPath() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenTtl(Duration.ofMinutes(30));
        AccessTokenCookieManager cookieManager =
                new AccessTokenCookieManager(false, jwtProperties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieManager.delete(response);

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("access_token=")
                .contains("Path=/")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }
}
