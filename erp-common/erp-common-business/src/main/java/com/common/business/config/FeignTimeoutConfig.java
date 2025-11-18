package com.common.business.config;

import feign.Request;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTimeoutConfig {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(120, TimeUnit.SECONDS,120,TimeUnit.SECONDS,true);
    }

}
