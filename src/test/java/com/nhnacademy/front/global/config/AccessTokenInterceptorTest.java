package com.nhnacademy.front.global.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessTokenInterceptorTest {

    @Mock
    private HttpRequest outgoingRequest;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse clientResponse;

    private AccessTokenInterceptor interceptor;
    private HttpHeaders headers;
    private byte[] body;

    @BeforeEach
    void setUp() {
        interceptor = new AccessTokenInterceptor();
        headers = new HttpHeaders();
        body = new byte[0];
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void refreshCallDoesNotForwardExistingAccessToken() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new jakarta.servlet.http.Cookie(
                "access_token",
                "expired-token"
        ));
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(servletRequest)
        );
        given(outgoingRequest.getURI()).willReturn(
                URI.create("http://localhost:10400/api/auth/refresh")
        );
        given(execution.execute(outgoingRequest, body)).willReturn(clientResponse);

        interceptor.intercept(outgoingRequest, body, execution);

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @Test
    void downstreamCallUsesAccessTokenCookie() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new jakarta.servlet.http.Cookie(
                "access_token",
                "access-token"
        ));
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(servletRequest)
        );
        given(outgoingRequest.getURI()).willReturn(
                URI.create("http://localhost:10400/api/accounts/me")
        );
        given(outgoingRequest.getHeaders()).willReturn(headers);
        given(execution.execute(outgoingRequest, body)).willReturn(clientResponse);

        interceptor.intercept(outgoingRequest, body, execution);

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer access-token");
    }
}
