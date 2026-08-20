package com.nhnacademy.front.global.config;

import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import com.nhnacademy.front.global.security.CookieAuthenticationEntryPoint;
import com.nhnacademy.front.global.security.PageAccessDeniedHandler;
import jakarta.servlet.http.Cookie;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter converter,
            BearerTokenResolver bearerTokenResolver,
            CookieAuthenticationEntryPoint authenticationEntryPoint,
            PageAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(bearerTokenResolver)
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(converter)
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("health", "serviceregistry")).permitAll()
                        .requestMatchers(
                                "/login",
                                "/signup",
                                "/check-email",
                                "/forgot-password",
                                "/pwd/**",
                                "/.well-known/jwks.json",
                                "/403",
                                "/404",
                                "/error",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/img/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        Assert.hasText(properties.getIssuer(), "security.jwt.issuer must be configured");
        Assert.notEmpty(properties.getAudiences(), "security.jwt.audiences must not be empty");
        Assert.notEmpty(properties.getAllowedAlgorithms(), "security.jwt.allowed-algorithms must not be empty");
        Assert.hasText(properties.getJwkSetUri(), "security.jwt.jwk-set-uri must not be empty");

        NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder =
                NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri());
        properties.getAllowedAlgorithms().forEach(builder::jwsAlgorithm);

        NimbusJwtDecoder decoder = builder.build();
        decoder.setJwtValidator(jwtValidator(properties));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("sub");
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return request -> {
            Cookie cookie = WebUtils.getCookie(request, AccessTokenCookieManager.COOKIE_NAME);
            return cookie != null && StringUtils.hasText(cookie.getValue())
                    ? cookie.getValue()
                    : null;
        };
    }

    private OAuth2TokenValidator<Jwt> jwtValidator(JwtProperties properties) {
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
        timestampValidator.setAllowEmptyExpiryClaim(false);

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(timestampValidator);
        validators.add(new JwtIssuerValidator(properties.getIssuer()));
        validators.add(jwt -> jwt.getAudience().stream().anyMatch(properties.getAudiences()::contains)
                ? OAuth2TokenValidatorResult.success()
                : validationFailure("JWT audience is not allowed"));
        validators.add(jwt -> jwt.getHeaders().get("kid") instanceof String kid && !kid.isBlank()
                ? OAuth2TokenValidatorResult.success()
                : validationFailure("JWT kid header is required"));
        validators.add(this::validateUuidSubject);

        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    private OAuth2TokenValidatorResult validateUuidSubject(Jwt jwt) {
        try {
            UUID.fromString(jwt.getSubject());
            return OAuth2TokenValidatorResult.success();
        } catch (IllegalArgumentException | NullPointerException exception) {
            return validationFailure("JWT subject must be an account UUID");
        }
    }

    private OAuth2TokenValidatorResult validationFailure(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
    }
}
