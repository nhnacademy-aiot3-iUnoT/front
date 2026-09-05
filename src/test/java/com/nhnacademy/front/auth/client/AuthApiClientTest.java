package com.nhnacademy.front.auth.client;

import com.nhnacademy.front.auth.dto.request.RefreshTokenRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthApiClientTest {

    @Mock
    private GatewayClient gatewayClient;

    @InjectMocks
    private AuthApiClient authApiClient;

    @Test
    void passwordResetRequestUsesBodilessAcceptedResponseContract() {
        ResetPasswordTokenRequest request =
                new ResetPasswordTokenRequest("user@example.com");

        authApiClient.passwordResetToken(request);

        then(gatewayClient).should().post("/api/accounts/pwd", request);
    }

    @Test
    void refreshUsesAccountTokenRotationContract() {
        RefreshTokenRequest request = new RefreshTokenRequest(
                "a".repeat(32) + "." + "b".repeat(64)
        );
        LoginResponse response = new LoginResponse("access-token", "refresh-token");
        given(gatewayClient.post(
                "/api/auth/refresh",
                request,
                LoginResponse.class
        )).willReturn(response);

        LoginResponse result = authApiClient.refresh(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void logoutUsesBodilessAccountRevocationContract() {
        RefreshTokenRequest request = new RefreshTokenRequest(
                "a".repeat(32) + "." + "b".repeat(64)
        );

        authApiClient.logout(request);

        then(gatewayClient).should().post("/api/auth/logout", request);
    }
}
