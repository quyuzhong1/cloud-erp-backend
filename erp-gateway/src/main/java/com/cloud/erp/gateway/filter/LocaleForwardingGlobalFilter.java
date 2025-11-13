package com.cloud.erp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 将请求头中的 Accept-Language 转发到下游服务
 * @author Cloud
 */
@Component
public class LocaleForwardingGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String lang = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        if (lang == null) {
            // 可按用户偏好/租户配置设置默认语言
            lang = "en-US";
            ServerHttpRequest mutated = exchange.getRequest()
                    .mutate()
                    .header("Accept-Language", lang)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        }
        return chain.filter(exchange);
    }
    @Override public int getOrder() { return -100; }
}

