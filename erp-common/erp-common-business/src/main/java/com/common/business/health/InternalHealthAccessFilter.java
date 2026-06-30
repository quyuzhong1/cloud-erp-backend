package com.common.business.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 保护业务服务内部探针路径，避免集群内任意工作负载读取发布/就绪细节。
 */
@Slf4j
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class InternalHealthAccessFilter implements Filter, Ordered {

    private static final String INTERNAL_PATH_PREFIX = "/internal/";
    private static final String LIVE_PATH = "/internal/live";
    private static final String READY_PATH = "/internal/ready";
    private static final String ALLOWED_PROBE_CIDRS_KEY = "erp.internal-health.allowed-probe-cidrs";
    private static final String DEFAULT_ALLOWED_PROBE_CIDRS =
            "10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,100.64.0.0/10,169.254.0.0/16";

    private final Environment environment;

    public InternalHealthAccessFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = getPathWithinApplication(httpRequest);
        if (!StringUtils.startsWith(path, INTERNAL_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String remoteAddress = httpRequest.getRemoteAddr();
        if (isLoopback(remoteAddress) || (isProbePath(path) && isAllowedProbeAddress(remoteAddress))) {
            chain.doFilter(request, response);
            return;
        }

        log.warn("Blocked internal health path access, path: {}, remoteAddress: {}", path, remoteAddress);
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isProbePath(String path) {
        return LIVE_PATH.equals(path) || READY_PATH.equals(path);
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (hasText(contextPath) && StringUtils.startsWith(requestUri, contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private boolean isLoopback(String remoteAddress) {
        return "127.0.0.1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress)
                || "::1".equals(remoteAddress);
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
