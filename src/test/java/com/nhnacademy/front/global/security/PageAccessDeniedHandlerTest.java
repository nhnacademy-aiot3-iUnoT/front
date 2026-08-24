package com.nhnacademy.front.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class PageAccessDeniedHandlerTest {

    private PageAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        accessDeniedHandler = new PageAccessDeniedHandler();
    }

    @Test
    void accessDeniedRedirectsToForbiddenPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/front");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("access denied")
        );

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/front/403");
    }
}
