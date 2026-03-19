package com.common.business.config;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = TraceContext.traceId();
            // 提取纯 traceId（去掉 .xxx.xxx）
            if (!"N/A".equals(traceId) && !"Ignored_Trace".equals(traceId)) {
                MDC.put(TRACE_ID_KEY, traceId);
            } else {
                MDC.put(TRACE_ID_KEY, "");
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
