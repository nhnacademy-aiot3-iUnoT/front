package com.nhnacademy.front.auth.client;

import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.global.client.GatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
