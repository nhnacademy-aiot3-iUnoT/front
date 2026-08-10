package com.nhnacademy.front.auth.dto.response;


import java.time.Instant;

public record LoginResponse(
    String accessToken,
    Instant expiresAt
) { }
