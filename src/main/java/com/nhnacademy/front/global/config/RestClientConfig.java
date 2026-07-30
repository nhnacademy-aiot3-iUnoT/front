package com.nhnacademy.front.global.config;

import jakarta.servlet.http.Cookie;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Configuration
public class RestClientConfig {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Bean
    @LoadBalanced
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    String path = request.getURI().getPath();

                    boolean publicEndpoint =
                            path.equals("/api/account/login")
                                    || path.equals("/api/account/signup")
                                    || path.equals("/api/account/check-email");

                    if (!publicEndpoint) {
                        var requestAttributes =
                                RequestContextHolder.getRequestAttributes();

                        if (requestAttributes instanceof ServletRequestAttributes attributes) {
                            Cookie[] cookies = attributes.getRequest().getCookies();

                            if (cookies != null) {
                                Arrays.stream(cookies)
                                        .filter(cookie ->
                                                ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                                        .map(Cookie::getValue)
                                        .filter(StringUtils::hasText)
                                        .findFirst()
                                        .ifPresent(token ->
                                                request.getHeaders().setBearerAuth(token));
                            }
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}