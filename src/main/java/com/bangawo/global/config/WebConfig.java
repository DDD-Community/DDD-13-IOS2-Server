package com.bangawo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

    /** 소셜 로그인 API 호출용 RestTemplate */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
