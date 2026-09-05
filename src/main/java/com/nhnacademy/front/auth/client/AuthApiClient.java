package com.nhnacademy.front.auth.client;

import com.nhnacademy.front.auth.dto.request.CheckEmailRequest;
import com.nhnacademy.front.auth.dto.request.LoginRequest;
import com.nhnacademy.front.auth.dto.request.RefreshTokenRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordRequest;
import com.nhnacademy.front.auth.dto.request.ResetPasswordTokenRequest;
import com.nhnacademy.front.auth.dto.request.SignupRequest;
import com.nhnacademy.front.auth.dto.response.CheckEmailResponse;
import com.nhnacademy.front.auth.dto.response.LoginResponse;
import com.nhnacademy.front.auth.dto.response.ResetPasswordResponse;
import com.nhnacademy.front.auth.dto.response.SignupResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

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

    public LoginResponse login(LoginRequest request) {
        return gatewayClient.post(AUTH_SERVICE + "/login", request, LoginResponse.class);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        return gatewayClient.post(
                AUTH_SERVICE + "/refresh",
                request,
                LoginResponse.class
        );
    }

    public void logout(RefreshTokenRequest request) {
        gatewayClient.post(AUTH_SERVICE + "/logout", request);
    }

    public SignupResponse signup(SignupRequest request) {
        return gatewayClient.post(ACCOUNT_SERVICE, request, SignupResponse.class);
    }

    public CheckEmailResponse checkEmail(CheckEmailRequest request) {
        return gatewayClient.post(
                ACCOUNT_SERVICE + "/check-email",
                request,
                CheckEmailResponse.class
        );
    }

    public void passwordResetToken(
            ResetPasswordTokenRequest request
    ) {
        gatewayClient.post(ACCOUNT_SERVICE + "/pwd", request);
    }

    public ResetPasswordResponse resetPassword(
            ResetPasswordRequest request,
            String token
    ) {
        return gatewayClient.post(
                ACCOUNT_SERVICE + "/pwd/reset/" + token,
                request,
                ResetPasswordResponse.class
        );
    }
}
