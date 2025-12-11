package com.erp.sdk.fs.config;

import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ServletConfig {
    @Bean
    public ServletAdapter getServletAdapter() {
        return new ServletAdapter();
    }
}