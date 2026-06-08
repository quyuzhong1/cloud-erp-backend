package com.common.business.config;

import com.common.business.utils.ApplicationContextUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Feign/Ribbon 服务发现按发布版本选择实例，避免蓝绿两色混调。
 */
public class ReleaseAwareRibbonRule extends AbstractLoadBalancerRule {

    private static final Logger log = LoggerFactory.getLogger(ReleaseAwareRibbonRule.class);
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private final AtomicInteger position = new AtomicInteger(0);
    private final Environment environment;
    private volatile Environment applicationEnvironment;
    private volatile String clientName;

    public ReleaseAwareRibbonRule() {
        this.environment = null;
    }

    public ReleaseAwareRibbonRule(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer loadBalancer = getLoadBalancer();
        if (loadBalancer == null) {
            return null;
        }
        List<Server> reachableServers = loadBalancer.getReachableServers();
        List<Server> candidates = filterByRelease(reachableServers);
        if (candidates.isEmpty()) {
            log.warn("No active release instance found for client={}, loadBalancer={}, activeColor={}, activeVersion={}, reachableServers={}",
                    clientName, loadBalancer, getActiveColor(), getActiveVersion(), reachableServers);
            return null;
        }
        return candidates.get(nextIndex(candidates.size()));
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        if (clientConfig != null) {
            this.clientName = clientConfig.getClientName();
        }
    }

    private List<Server> filterByRelease(List<Server> servers) {
        if (servers == null || servers.isEmpty()) {
            return Collections.emptyList();
        }

        String activeColor = getActiveColor();
        String activeVersion = getActiveVersion();
        if (!hasText(activeColor) && !hasText(activeVersion)) {
            return servers;
        }

        List<Server> matched = new ArrayList<>();
        for (Server server : servers) {
            Map<String, String> metadata = getMetadata(server);
            if (metadata.isEmpty()) {
                continue;
            }
            if (!matchesService(metadata)) {
                log.warn("Skip Ribbon server [{}] because service metadata does not match client [{}], metadata={}",
                        server, clientName, metadata);
                continue;
            }
            String releaseColor = firstText(metadata, "release.color", "release-color", "releaseColor", "color");
            String releaseVersion = firstText(metadata, "release.version", "release-version", "releaseVersion", "version");
            if (matchesRelease(activeColor, activeVersion, releaseColor, releaseVersion)) {
                matched.add(server);
            }
        }

        return matched;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getMetadata(Server server) {
        try {
            Method getMetadata = server.getClass().getMethod("getMetadata");
            Object metadata = getMetadata.invoke(server);
            if (metadata instanceof Map) {
                return (Map<String, String>) metadata;
            }
        } catch (Exception ignored) {
            // Different Ribbon Server implementations expose metadata differently.
        }
        try {
            Method getInstance = server.getClass().getMethod("getInstance");
            Object instance = getInstance.invoke(server);
            Method getMetadata = instance.getClass().getMethod("getMetadata");
            Object metadata = getMetadata.invoke(instance);
            if (metadata instanceof Map) {
                return (Map<String, String>) metadata;
            }
        } catch (Exception ignored) {
            // Non-Nacos server, treat as legacy metadata-less instance.
        }
        return Collections.emptyMap();
    }

    private String getActiveColor() {
        Environment env = getEnvironment();
        return env == null ? null : env.getProperty(ACTIVE_COLOR_KEY);
    }

    private String getActiveVersion() {
        Environment env = getEnvironment();
        return env == null ? null : env.getProperty(ACTIVE_VERSION_KEY);
    }

    private Environment getEnvironment() {
        Environment env = applicationEnvironment;
        if (env != null) {
            return env;
        }
        try {
            env = ApplicationContextUtils.getBean(Environment.class);
            applicationEnvironment = env;
            return env;
        } catch (Exception e) {
            if (environment == null) {
                log.warn("Cannot get Environment for release-aware Ribbon rule, fallback to normal Ribbon choose.", e);
            }
            return environment;
        }
    }

    private int nextIndex(int size) {
        return Math.floorMod(position.getAndIncrement(), size);
    }

    private String firstText(Map<String, String> metadata, String... keys) {
        for (String key : keys) {
            String value = metadata.get(key);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean matchesService(Map<String, String> metadata) {
        if (!hasText(clientName)) {
            return true;
        }
        String serviceName = firstText(metadata, "service", "serviceName", "service-name", "spring.application.name");
        if (!hasText(serviceName)) {
            return true;
        }
        return normalizeServiceName(clientName).equalsIgnoreCase(normalizeServiceName(serviceName));
    }

    private boolean matchesRelease(String activeColor, String activeVersion, String releaseColor, String releaseVersion) {
        boolean hasComparableMetadata = false;
        if (hasText(activeColor) && hasText(releaseColor)) {
            hasComparableMetadata = true;
            if (!activeColor.equalsIgnoreCase(releaseColor)) {
                return false;
            }
        }
        if (hasText(activeVersion) && hasText(releaseVersion)) {
            hasComparableMetadata = true;
            if (!activeVersion.equalsIgnoreCase(releaseVersion)) {
                return false;
            }
        }
        return hasComparableMetadata;
    }

    private String normalizeServiceName(String serviceName) {
        if (!hasText(serviceName)) {
            return "";
        }
        String normalized = serviceName.trim();
        int groupSeparator = normalized.indexOf("@@");
        if (groupSeparator >= 0 && groupSeparator + 2 < normalized.length()) {
            normalized = normalized.substring(groupSeparator + 2);
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
