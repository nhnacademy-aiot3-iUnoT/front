package com.nhnacademy.front.auth.service;

import com.nhnacademy.front.auth.client.AuthApiClient;
import com.nhnacademy.front.auth.dto.request.RefreshTokenRequest;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import com.nhnacademy.front.global.security.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final AuthApiClient authApiClient;
    private final AccessTokenCookieManager accessTokenCookieManager;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    public void establish(HttpServletResponse response, LoginResponse tokens) {
        accessTokenCookieManager.add(response, tokens.accessToken());
        refreshTokenCookieManager.add(response, tokens.refreshToken());
        log.info("event=refresh_token_cookie_stored");
    }

    public String renew(HttpServletResponse response, String refreshToken) {
        LoginResponse tokens = authApiClient.refresh(
                new RefreshTokenRequest(refreshToken)
        );

        if (!StringUtils.hasText(tokens.accessToken())
                || !StringUtils.hasText(tokens.refreshToken())) {
            throw new IllegalStateException("Token refresh response is incomplete");
        }

        establish(response, tokens);
        return tokens.accessToken();
    }

    public void revoke(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = refreshTokenCookieManager.resolve(request);

        try {
            if (StringUtils.hasText(refreshToken)) {
                authApiClient.logout(new RefreshTokenRequest(refreshToken));
                log.info("event=refresh_token_revoke_succeeded");
            } else {
                log.debug(
                        "event=refresh_token_revoke_skipped reason=missing_cookie"
                );
            }
        } catch (RuntimeException exception) {
            log.warn(
                    "event=refresh_token_revoke_failed exceptionType={}",
                    exception.getClass().getSimpleName()
            );
        } finally {
            clear(response);
        }
    }

    public void clear(HttpServletResponse response) {
        accessTokenCookieManager.delete(response);
        refreshTokenCookieManager.delete(response);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return accessTokenCookieManager.resolve(request);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return refreshTokenCookieManager.resolve(request);
    }
}
