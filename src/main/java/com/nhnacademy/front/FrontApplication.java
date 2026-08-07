package com.nhnacademy.front;

import com.nhnacademy.front.global.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FrontApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(FrontApplication.class, args);
    }

}
