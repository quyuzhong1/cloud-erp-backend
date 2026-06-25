package com.erp.server.sys.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SSE 依赖 Servlet 3 的异步能力。
 * 某些分支会额外注册自定义 Filter，如果没有显式打开 asyncSupported，
 * SseEmitter 在 startAsync 时会直接抛 IllegalStateException。
 */
@Slf4j
@Configuration
public class AsyncRequestSupportConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ServletContextInitializer asyncRequestSupportInitializer() {
        return servletContext -> {
            List<String> servletNames = enableServletAsyncSupport(servletContext);
            List<String> filterNames = enableFilterAsyncSupport(servletContext);
            log.info("Enable async support for servlet/filter registrations, servletCount={}, filterCount={}, servlets={}, filters={}",
                    servletNames.size(), filterNames.size(), servletNames, filterNames);
        };
    }

    private List<String> enableServletAsyncSupport(ServletContext servletContext) {
        List<String> servletNames = new ArrayList<>();
        for (Map.Entry<String, ? extends ServletRegistration> entry : servletContext.getServletRegistrations().entrySet()) {
            ServletRegistration registration = entry.getValue();
            if (registration instanceof ServletRegistration.Dynamic) {
                ((ServletRegistration.Dynamic) registration).setAsyncSupported(true);
                servletNames.add(entry.getKey());
            }
        }
        return servletNames;
    }

    private List<String> enableFilterAsyncSupport(ServletContext servletContext) {
        List<String> filterNames = new ArrayList<>();
        for (Map.Entry<String, ? extends FilterRegistration> entry : servletContext.getFilterRegistrations().entrySet()) {
            FilterRegistration registration = entry.getValue();
            if (registration instanceof FilterRegistration.Dynamic) {
                ((FilterRegistration.Dynamic) registration).setAsyncSupported(true);
                filterNames.add(entry.getKey());
            }
        }
        return filterNames;
    }

}
