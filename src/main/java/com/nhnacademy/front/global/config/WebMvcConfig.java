package com.nhnacademy.front.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final InactiveAccountInterceptor inactiveAccountInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(inactiveAccountInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/reactivation",
                        "/reactivation/**",
                        "/refresh",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/favicon.ico",
                        "/error",
                        "/actuator/**"
                );
    }
}
