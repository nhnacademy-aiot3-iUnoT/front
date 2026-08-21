package com.nhnacademy.front.account.client;

import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.dto.response.UpdateAccountResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountApiClient {
    private final GatewayClient backendApiClient;
    private static final String ACCOUNT_SERVICE = "/api/accounts";

    public AccountInfoResponse getAccountInfo() {
        return backendApiClient.get(ACCOUNT_SERVICE + "/me", AccountInfoResponse.class);
    }

    public void withdraw(@Valid WithdrawAccountRequest request) {
        backendApiClient.delete(ACCOUNT_SERVICE + "/me");
    }

    public UpdateAccountResponse changeName(@Valid UpdateAccountNameRequest request) {
        return backendApiClient.put(ACCOUNT_SERVICE + "/me", request, UpdateAccountResponse.class);
    }

    public UpdateAccountResponse changePassword(@Valid ChangeOwnPasswordRequest request) {
        return backendApiClient.put(ACCOUNT_SERVICE + "/me/pwd", request, UpdateAccountResponse.class);
    }

}
