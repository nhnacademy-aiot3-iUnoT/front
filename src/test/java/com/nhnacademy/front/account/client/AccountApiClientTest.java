package com.nhnacademy.front.account.client;

import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.AccountStatus;
import com.nhnacademy.front.account.dto.request.ReactivationConfirmRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountApiClientTest {

    @Mock
    private GatewayClient gatewayClient;

    @InjectMocks
    private AccountApiClient accountApiClient;

    @Test
    void requestReactivationVerificationPostsWithoutBody() {
        accountApiClient.requestReactivationVerification();

        then(gatewayClient).should().post(
                "/api/accounts/me/reactivation/verification"
        );
    }

    @Test
    void confirmReactivationPostsJsonContractAndReturnsAccount() {
        ReactivationConfirmRequest request =
                new ReactivationConfirmRequest("a".repeat(64));
        AccountResponse response = new AccountResponse(
                UUID.randomUUID(),
                "사용자",
                "user@example.com",
                AccountRole.USER,
                AccountStatus.ACTIVE,
                LocalDateTime.of(2026, 8, 25, 12, 0),
                LocalDateTime.of(2026, 8, 25, 12, 5),
                null
        );
        given(gatewayClient.post(
                "/api/accounts/me/reactivation/confirm",
                request,
                AccountResponse.class
        )).willReturn(response);

        AccountResponse result = accountApiClient.confirmReactivation(request);

        assertSame(response, result);
        then(gatewayClient).should().post(
                "/api/accounts/me/reactivation/confirm",
                request,
                AccountResponse.class
        );
    }

    @Test
    void withdrawSendsPasswordInDeleteBody() {
        WithdrawAccountRequest request = new WithdrawAccountRequest("password");

        accountApiClient.withdraw(request);

        then(gatewayClient).should().delete("/api/accounts/me", request);
    }
}
