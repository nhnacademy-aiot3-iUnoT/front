package com.nhnacademy.front.global.security;

import com.nhnacademy.front.auth.service.AuthSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenAutoRenewFilter extends OncePerRequestFilter {

    public static final String REFRESHED_ACCESS_TOKEN_ATTRIBUTE =
            RefreshTokenAutoRenewFilter.class.getName() + ".refreshedAccessToken";
    public static final String REFRESH_FAILED_ATTRIBUTE =
            RefreshTokenAutoRenewFilter.class.getName() + ".refreshFailed";

    private static final Duration REFRESH_WINDOW = Duration.ofSeconds(30);
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/login",
            "/logout",
            "/signup",
            "/check-email",
            "/forgot-password",
            "/pwd",
            "/.well-known/jwks.json",
            "/403",
            "/404",
            "/error",
            "/favicon.ico"
    );

    private final JwtDecoder jwtDecoder;
    private final AuthSessionService authSessionService;
    private final Clock clock;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = authSessionService.resolveAccessToken(request);
        TokenState tokenState = inspect(accessToken);

        if (tokenState == TokenState.VALID) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = authSessionService.resolveRefreshToken(request);
        if (!StringUtils.hasText(refreshToken)) {
            log.debug(
                    "event=refresh_token_rotation_skipped reason=missing_cookie "
                            + "accessTokenState={} method={} path={}",
                    tokenState,
                    request.getMethod(),
                    request.getServletPath()
            );
            filterChain.doFilter(request, response);
            return;
        }

        log.info(
                "event=refresh_token_rotation_started accessTokenState={} method={} path={}",
                tokenState,
                request.getMethod(),
                request.getServletPath()
        );

        try {
            String refreshedAccessToken = authSessionService.renew(
                    response,
                    refreshToken
            );
            request.setAttribute(
                    REFRESHED_ACCESS_TOKEN_ATTRIBUTE,
                    refreshedAccessToken
            );

            log.info(
                    "event=refresh_token_rotation_succeeded accessTokenState={} method={} path={}",
                    tokenState,
                    request.getMethod(),
                    request.getServletPath()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "event=refresh_token_rotation_failed accessTokenState={} method={} path={} "
                            + "exceptionType={}",
                    tokenState,
                    request.getMethod(),
                    request.getServletPath(),
                    exception.getClass().getSimpleName()
            );

            if (tokenState != TokenState.EXPIRING) {
                request.setAttribute(REFRESH_FAILED_ATTRIBUTE, Boolean.TRUE);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();
        return PUBLIC_PATHS.contains(path)
                || ("POST".equalsIgnoreCase(request.getMethod())
                    && "/juso/popup".equals(path))
                || path.startsWith("/pwd/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/actuator/");
    }

    private TokenState inspect(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return TokenState.MISSING;
        }

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            Instant expiresAt = jwt.getExpiresAt();
            Instant now = clock.instant();

            if (expiresAt == null || !expiresAt.isAfter(now)) {
                return TokenState.EXPIRED;
            }

            return expiresAt.isAfter(now.plus(REFRESH_WINDOW))
                    ? TokenState.VALID
                    : TokenState.EXPIRING;
        } catch (JwtException | IllegalArgumentException exception) {
            return TokenState.INVALID;
        }
    }

    private enum TokenState {
        VALID,
        EXPIRING,
        EXPIRED,
        INVALID,
        MISSING
    }
}
