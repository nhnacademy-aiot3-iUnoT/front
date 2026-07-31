package com.nhnacademy.front.global.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AccessTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
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

        return execution.execute(request, body);
    }

}
