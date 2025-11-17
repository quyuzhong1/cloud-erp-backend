package com.common.business.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTimeoutConfig {

    @Bean
    public Request.Options FeignTimeoutConfig() {
        return new Request.Options(30000, 120000);
    }

}
