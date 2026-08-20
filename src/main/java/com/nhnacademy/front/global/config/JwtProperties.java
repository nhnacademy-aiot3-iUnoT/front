package com.nhnacademy.front.global.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Validated
@ConfigurationProperties("security.jwt")
public class JwtProperties {

    @Setter
    @NotBlank
    private String issuer;

    @NotEmpty
    private Set<@NotBlank String> audiences = new LinkedHashSet<>();

    @Setter
    @NotNull
    private Duration accessTokenTtl;

    @Setter
    @NotBlank
    private String jwkSetUri;

    @Setter
    @NotEmpty
    private Set<SignatureAlgorithm> allowedAlgorithms =
            new LinkedHashSet<>(Set.of(SignatureAlgorithm.RS256));

    public void setAudiences(Set<String> audiences) {
        this.audiences = audiences == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(audiences);
    }

    @AssertTrue(message = "security.jwt.access-token-ttl must be positive")
    public boolean isAccessTokenTtlPositive() {
        return accessTokenTtl == null || accessTokenTtl.compareTo(Duration.ZERO) > 0;
    }
}
