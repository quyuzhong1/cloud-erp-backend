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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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
            return matchesInetCidr(remoteAddress, network, prefix);
        } catch (NumberFormatException | UnknownHostException ex) {
            return false;
        }
    }

    private boolean matchesInetCidr(String remoteAddress, String network, int prefix) throws UnknownHostException {
        if (!isIpLiteral(remoteAddress) || !isIpLiteral(network)) {
            return false;
        }
        byte[] remoteBytes = InetAddress.getByName(remoteAddress).getAddress();
        byte[] networkBytes = InetAddress.getByName(network).getAddress();
        if (remoteBytes.length != networkBytes.length) {
            return false;
        }
        int maxPrefix = remoteBytes.length * 8;
        if (prefix < 0 || prefix > maxPrefix) {
            return false;
        }
        int fullBytes = prefix / 8;
        for (int i = 0; i < fullBytes; i++) {
            if (remoteBytes[i] != networkBytes[i]) {
                return false;
            }
        }
        int remainingBits = prefix % 8;
        if (remainingBits == 0) {
            return true;
        }
        int mask = (0xff << (8 - remainingBits)) & 0xff;
        return (remoteBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
    }

    private boolean isIpLiteral(String address) {
        if (!hasText(address)) {
            return false;
        }
        if (StringUtils.contains(address, ":")) {
            return true;
        }
        return isStrictIpv4(address);
    }

    private boolean isStrictIpv4(String address) {
        String[] parts = address.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (!hasText(part)) {
                return false;
            }
            for (int i = 0; i < part.length(); i++) {
                if (!Character.isDigit(part.charAt(i))) {
                    return false;
                }
            }
            int value = Integer.parseInt(part);
            if (value < 0 || value > 255) {
                return false;
            }
        }
        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
