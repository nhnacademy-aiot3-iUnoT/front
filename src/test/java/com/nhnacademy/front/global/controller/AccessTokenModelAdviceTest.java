package com.nhnacademy.front.global.controller;

import com.nhnacademy.front.global.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenModelAdviceTest {

    private static final Instant EXPIRES_AT = Instant.parse("2026-09-03T09:30:00Z");

    private AccessTokenModelAdvice advice;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(30));
        advice = new AccessTokenModelAdvice(properties);
    }

    @Test
    void addsConfiguredDurationAndJwtExpiration() {
        ExtendedModelMap model = new ExtendedModelMap();
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .subject("account")
                .issuedAt(EXPIRES_AT.minusSeconds(1800))
                .expiresAt(EXPIRES_AT)
                .build();

        advice.addAccessTokenExpiration(
                new JwtAuthenticationToken(jwt, List.of()),
                model
        );

        assertThat(model.get("accessTokenDurationSeconds")).isEqualTo(1800L);
        assertThat(model.get("accessTokenExpiresAt")).isEqualTo(EXPIRES_AT.toEpochMilli());
    }

    @Test
    void anonymousRequestOnlyGetsConfiguredDuration() {
        ExtendedModelMap model = new ExtendedModelMap();

        advice.addAccessTokenExpiration(null, model);

        assertThat(model.get("accessTokenDurationSeconds")).isEqualTo(1800L);
        assertThat(model).doesNotContainKey("accessTokenExpiresAt");
    }
}
