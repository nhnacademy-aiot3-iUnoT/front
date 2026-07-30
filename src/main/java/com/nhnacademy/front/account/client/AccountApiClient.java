package com.nhnacademy.front.account.client;

import com.nhnacademy.front.account.dto.request.CheckEmailRequest;
import com.nhnacademy.front.account.dto.request.LoginRequest;
import com.nhnacademy.front.account.dto.request.SignupRequest;
import com.nhnacademy.front.account.dto.response.CheckEmailResponse;
import com.nhnacademy.front.account.dto.response.LoginResponse;
import com.nhnacademy.front.account.dto.response.SignupResponse;
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
}

