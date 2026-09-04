package com.nhnacademy.front.auth.service;

import com.nhnacademy.front.auth.client.AuthApiClient;
import com.nhnacademy.front.auth.dto.request.RefreshTokenRequest;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import com.nhnacademy.front.global.security.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class AuthSessionServiceTest {

    @Mock
    private AuthApiClient authApiClient;

    @Mock
    private AccessTokenCookieManager accessTokenCookieManager;

    @Mock
    private RefreshTokenCookieManager refreshTokenCookieManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthSessionService authSessionService;

    @Test
    void establishStoresBothTokensWithoutLoggingTheirValues(CapturedOutput output) {
        LoginResponse tokens = new LoginResponse("access-token", "refresh-token");

        authSessionService.establish(response, tokens);

        then(accessTokenCookieManager).should().add(response, "access-token");
        then(refreshTokenCookieManager).should().add(response, "refresh-token");
        assertThat(output)
                .contains("event=refresh_token_cookie_stored")
                .doesNotContain("access-token")
                .doesNotContain("refresh-token");
    }

    @Test
    void renewCallsAccountAndStoresRotatedTokens(CapturedOutput output) {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("old-refresh-token");
        LoginResponse tokens = new LoginResponse("new-access-token", "new-refresh-token");
        given(refreshTokenCookieManager.resolve(request)).willReturn("old-refresh-token");
        given(authApiClient.refresh(refreshRequest)).willReturn(tokens);

        boolean renewed = authSessionService.renew(request, response);

        assertThat(renewed).isTrue();
        then(accessTokenCookieManager).should().add(response, "new-access-token");
        then(refreshTokenCookieManager).should().add(response, "new-refresh-token");
        assertThat(output)
                .contains("event=refresh_token_rotation_started")
                .contains("event=refresh_token_rotation_succeeded")
                .doesNotContain("old-refresh-token")
                .doesNotContain("new-access-token")
                .doesNotContain("new-refresh-token");
    }

    @Test
    void renewReturnsFalseWhenRefreshCookieIsMissing() {
        given(refreshTokenCookieManager.resolve(request)).willReturn(null);

        boolean renewed = authSessionService.renew(request, response);

        assertThat(renewed).isFalse();
        then(authApiClient).shouldHaveNoInteractions();
        then(accessTokenCookieManager).shouldHaveNoInteractions();
    }

    @Test
    void renewRejectsIncompleteResponseBeforeWritingCookies() {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("old-refresh-token");
        given(refreshTokenCookieManager.resolve(request)).willReturn("old-refresh-token");
        given(authApiClient.refresh(refreshRequest))
                .willReturn(new LoginResponse("new-access-token", ""));

        assertThatThrownBy(() -> authSessionService.renew(request, response))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token refresh response is incomplete");

        then(accessTokenCookieManager).shouldHaveNoInteractions();
        then(refreshTokenCookieManager).should().resolve(request);
        then(refreshTokenCookieManager).shouldHaveNoMoreInteractions();
    }

    @Test
    void revokeCallsAccountAndClearsBothCookies(CapturedOutput output) {
        given(refreshTokenCookieManager.resolve(request)).willReturn("refresh-token");

        authSessionService.revoke(request, response);

        then(authApiClient).should().logout(new RefreshTokenRequest("refresh-token"));
        then(accessTokenCookieManager).should().delete(response);
        then(refreshTokenCookieManager).should().delete(response);
        assertThat(output)
                .contains("event=refresh_token_revoke_succeeded")
                .doesNotContain("refresh-token");
    }

    @Test
    void revokeStillClearsCookiesWhenAccountCallFails(CapturedOutput output) {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("refresh-token");
        given(refreshTokenCookieManager.resolve(request)).willReturn("refresh-token");
        willThrow(new IllegalStateException("account unavailable"))
                .given(authApiClient)
                .logout(refreshRequest);

        authSessionService.revoke(request, response);

        then(accessTokenCookieManager).should().delete(response);
        then(refreshTokenCookieManager).should().delete(response);
        assertThat(output)
                .contains("event=refresh_token_revoke_failed")
                .contains("exceptionType=IllegalStateException")
                .doesNotContain("refresh-token");
    }
}
