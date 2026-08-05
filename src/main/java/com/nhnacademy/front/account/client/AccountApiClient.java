package com.nhnacademy.front.account.client;

import com.nhnacademy.front.account.dto.request.*;
import com.nhnacademy.front.account.dto.response.*;
import com.nhnacademy.front.global.client.GatewayClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountApiClient {
    private final GatewayClient backendApiClient;
    private static final String ACCOUNT_SERVICE = "/api/accounts";
    private static final String AUTH_SERVICE = "/api/auth";

    public LoginResponse login(@Valid LoginRequest request) {
        return backendApiClient.post(AUTH_SERVICE + "/login", request, LoginResponse.class);
    }

    public SignupResponse signup(@Valid SignupRequest request) {
        return backendApiClient.post(ACCOUNT_SERVICE, request, SignupResponse.class);
    }

    public CheckEmailResponse checkEmail(@Valid CheckEmailRequest request) {
        return backendApiClient.post(ACCOUNT_SERVICE + "/check-email", request, CheckEmailResponse.class);
    }

    public AccountInfoResponse getAccountInfo() {
        return backendApiClient.get(ACCOUNT_SERVICE + "/me", AccountInfoResponse.class);
    }

    public WithdrawAccountResponse withdraw(@Valid WithdrawAccountRequest request) {
        return backendApiClient.delete(ACCOUNT_SERVICE + "/me", null);
    }

    public UpdateAccountResponse changeName(@Valid UpdateAccountNameRequest request) {
        return backendApiClient.put(ACCOUNT_SERVICE + "/me", request, UpdateAccountResponse.class);
    }

    public UpdateAccountResponse changePassword(@Valid UpdateAccountPasswordRequest request) {
        return backendApiClient.put(ACCOUNT_SERVICE + "/me/pwd", request, UpdateAccountResponse.class);
    }

    public ResetPasswordTokenResponse passwordResetToken(@Valid ResetPasswordTokenRequest request) {
        return backendApiClient.post(ACCOUNT_SERVICE + "/pwd", request, ResetPasswordTokenResponse.class);
    }

    public UpdateAccountResponse resetPassword(@Valid UpdateAccountPasswordRequest request, String token) {
        return backendApiClient.post(
                ACCOUNT_SERVICE + "/pwd/reset/" + token, request, UpdateAccountResponse.class
        );
    }
}
