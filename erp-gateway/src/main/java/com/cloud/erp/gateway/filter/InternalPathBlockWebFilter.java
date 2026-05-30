package com.cloud.erp.gateway.filter;

import com.cloud.erp.gateway.utils.ServletUtils;
import com.common.core.enums.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 阻止外部用户访问 Gateway 本地探针路径，K8s 需通过容器内 127.0.0.1 探测。
 */
@Slf4j
@Component
public class InternalPathBlockWebFilter implements WebFilter, Ordered {

    private static final String INTERNAL_PATH_PREFIX = "/internal/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        if (!StringUtils.startsWith(path, INTERNAL_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String remoteAddress = getRemoteAddress(request);
        if (isLoopback(remoteAddress)) {
            return chain.filter(exchange);
        }

        log.warn("Blocked external internal path access, path: {}, remoteAddress: {}", path, remoteAddress);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), "Not Found",
                ApiError.HTTP_NOT_FOUND.getCode());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String getRemoteAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return null;
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    private boolean isLoopback(String remoteAddress) {
        return "127.0.0.1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress)
                || "::1".equals(remoteAddress);
    }
}
