package com.nhnacademy.front.global.security;

import com.nhnacademy.front.auth.service.AuthSessionService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.ResourceAccessException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class RefreshTokenAutoRenewFilterTest {

    private static final Instant NOW = Instant.parse("2026-08-27T08:00:00Z");

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private FilterChain filterChain;

    private RefreshTokenAutoRenewFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RefreshTokenAutoRenewFilter(
                jwtDecoder,
                authSessionService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void missingAccessTokenIsRenewedBeforeSecurityAuthentication(
            CapturedOutput output
    ) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setServletPath("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authSessionService.resolveRefreshToken(request)).willReturn("old-refresh");
        given(authSessionService.renew(response, "old-refresh"))
                .willReturn("new-access");

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(
                RefreshTokenAutoRenewFilter.REFRESHED_ACCESS_TOKEN_ATTRIBUTE
        )).isEqualTo("new-access");
        then(authSessionService).should().renew(response, "old-refresh");
        then(filterChain).should().doFilter(request, response);
        assertThat(output)
                .contains(
                        "event=refresh_token_rotation_started "
                                + "accessTokenState=MISSING method=GET path=/"
                )
                .contains(
                        "event=refresh_token_rotation_succeeded "
                                + "accessTokenState=MISSING method=GET path=/"
                )
                .doesNotContain("old-refresh")
                .doesNotContain("new-access");
    }

    @Test
    void validAccessTokenDoesNotCallRefreshApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setServletPath("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authSessionService.resolveAccessToken(request)).willReturn("access-token");
        given(jwtDecoder.decode("access-token")).willReturn(jwt(NOW.plusSeconds(300)));

        filter.doFilter(request, response, filterChain);

        then(authSessionService).should(never()).renew(any(), anyString());
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void refreshFailureMarksRequestUnauthenticatedWhenAccessIsMissing(
            CapturedOutput output
    ) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setServletPath("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authSessionService.resolveRefreshToken(request)).willReturn("old-refresh");
        given(authSessionService.renew(response, "old-refresh"))
                .willThrow(new ResourceAccessException("gateway unavailable"));

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(
                RefreshTokenAutoRenewFilter.REFRESH_FAILED_ATTRIBUTE
        )).isEqualTo(Boolean.TRUE);
        then(filterChain).should().doFilter(request, response);
        assertThat(output)
                .contains(
                        "event=refresh_token_rotation_failed "
                                + "accessTokenState=MISSING method=GET path=/ "
                                + "exceptionType=ResourceAccessException"
                )
                .doesNotContain("old-refresh");
    }

    @Test
    void expiringAccessRemainsUsableWhenProactiveRefreshFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setServletPath("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authSessionService.resolveAccessToken(request)).willReturn("access-token");
        given(jwtDecoder.decode("access-token")).willReturn(jwt(NOW.plusSeconds(10)));
        given(authSessionService.resolveRefreshToken(request)).willReturn("old-refresh");
        given(authSessionService.renew(response, "old-refresh"))
                .willThrow(new ResourceAccessException("gateway unavailable"));

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(
                RefreshTokenAutoRenewFilter.REFRESH_FAILED_ATTRIBUTE
        )).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void publicPostPathSkipsRefreshPolicy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/juso/popup");
        request.setServletPath("/juso/popup");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        then(authSessionService).shouldHaveNoInteractions();
        then(filterChain).should().doFilter(request, response);
    }

    private Jwt jwt(Instant expiresAt) {
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(NOW.minusSeconds(60))
                .expiresAt(expiresAt)
                .claim("account_status", "ACTIVE")
                .build();
    }
}
