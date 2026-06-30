package com.cloud.erp.gateway.filter;

import com.cloud.erp.gateway.utils.ServletUtils;
import com.common.core.enums.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
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
    private static final String LIVE_PATH = "/internal/live";
    private static final String READY_PATH = "/internal/ready";
    private static final String ALLOWED_PROBE_CIDRS_KEY = "erp.internal-health.allowed-probe-cidrs";
    private static final String DEFAULT_ALLOWED_PROBE_CIDRS =
            "10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,100.64.0.0/10,169.254.0.0/16";

    private final Environment environment;

    public InternalPathBlockWebFilter(Environment environment) {
        this.environment = environment;
    }

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
        if (isProbePath(path) && isAllowedProbeAddress(remoteAddress)) {
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

    private boolean isProbePath(String path) {
        return LIVE_PATH.equals(path) || READY_PATH.equals(path);
    }

    private boolean isAllowedProbeAddress(String remoteAddress) {
        if (!hasText(remoteAddress)) {
            return false;
        }
        String allowedCidrs = environment.getProperty(ALLOWED_PROBE_CIDRS_KEY, DEFAULT_ALLOWED_PROBE_CIDRS);
        String[] cidrs = StringUtils.split(allowedCidrs, ',');
        if (cidrs == null) {
            return false;
        }
        for (String cidr : cidrs) {
            if (matchesCidr(remoteAddress, StringUtils.trim(cidr))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCidr(String remoteAddress, String cidr) {
        if (!hasText(cidr)) {
            return false;
        }
        if (!StringUtils.contains(cidr, "/")) {
            return remoteAddress.equals(cidr);
        }
        String network = StringUtils.substringBefore(cidr, "/");
        String prefixText = StringUtils.substringAfter(cidr, "/");
        try {
            int prefix = Integer.parseInt(prefixText);
            if (prefix < 0 || prefix > 32) {
                return false;
            }
            long remote = ipv4ToLong(remoteAddress);
            long networkValue = ipv4ToLong(network);
            long mask = prefix == 0 ? 0L : (0xffffffffL << (32 - prefix)) & 0xffffffffL;
            return (remote & mask) == (networkValue & mask);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private long ipv4ToLong(String address) {
        String[] parts = StringUtils.split(address, '.');
        if (parts == null || parts.length != 4) {
            throw new NumberFormatException("invalid ipv4 address");
        }
        long result = 0L;
        for (String part : parts) {
            int value = Integer.parseInt(part);
            if (value < 0 || value > 255) {
                throw new NumberFormatException("invalid ipv4 address");
            }
            result = (result << 8) + value;
        }
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
