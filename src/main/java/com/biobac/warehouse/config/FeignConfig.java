package com.biobac.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public FeignAuthInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}

