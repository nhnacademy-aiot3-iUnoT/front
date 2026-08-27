package com.nhnacademy.front.auth.client;

import com.nhnacademy.front.auth.dto.request.CheckEmailRequest;
import com.nhnacademy.front.auth.dto.request.LoginRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.request.SignupRequest;
import com.nhnacademy.front.auth.dto.response.CheckEmailResponse;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.auth.dto.response.ResetPasswordResponse;
import com.nhnacademy.front.auth.dto.response.SignupResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthApiClient {

    private static final String ACCOUNT_SERVICE = "/api/accounts";
    private static final String AUTH_SERVICE = "/api/auth";
    private static final String JWKS_PATH = AUTH_SERVICE + "/.well-known/jwks.json";

    private final GatewayClient gatewayClient;

    public Map<String, Object> jwks() {
        return gatewayClient.getRaw(JWKS_PATH, new ParameterizedTypeReference<>() {});
    }

    public LoginResponse login(@Valid LoginRequest request) {
        return gatewayClient.post(AUTH_SERVICE + "/login", request, LoginResponse.class);
    }

    public SignupResponse signup(@Valid SignupRequest request) {
        return gatewayClient.post(ACCOUNT_SERVICE, request, SignupResponse.class);
    }

    public CheckEmailResponse checkEmail(@Valid CheckEmailRequest request) {
        return gatewayClient.post(
                ACCOUNT_SERVICE + "/check-email",
                request,
                CheckEmailResponse.class
        );
    }

    public void passwordResetToken(
            @Valid ResetPasswordTokenRequest request
    ) {
        gatewayClient.post(ACCOUNT_SERVICE + "/pwd", request);
    }

    public ResetPasswordResponse resetPassword(
            @Valid ResetPasswordRequest request,
            String token
    ) {
        return gatewayClient.post(
                ACCOUNT_SERVICE + "/pwd/reset/" + token,
                request,
                ResetPasswordResponse.class
        );
    }
}
