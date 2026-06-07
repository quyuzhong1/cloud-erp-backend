package com.cloud.erp.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * Gateway lb:// 转发前按发布版本选择实例，避免外部入口跨蓝绿版本转发。
 */
@Component
public class ReleaseAwareGatewayLoadBalancerFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ReleaseAwareGatewayLoadBalancerFilter.class);
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private static final int ORDER_BEFORE_GATEWAY_LOAD_BALANCER = 10050;

    private final ReactiveDiscoveryClient discoveryClient;
    private final Environment environment;
    private final ConcurrentHashMap<String, AtomicInteger> positions = new ConcurrentHashMap<>();

    public ReleaseAwareGatewayLoadBalancerFilter(ReactiveDiscoveryClient discoveryClient, Environment environment) {
        this.discoveryClient = discoveryClient;
        this.environment = environment;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUrl = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        if (requestUrl == null || !"lb".equalsIgnoreCase(requestUrl.getScheme())) {
            return chain.filter(exchange);
        }

        String activeColor = environment.getProperty(ACTIVE_COLOR_KEY);
        String activeVersion = environment.getProperty(ACTIVE_VERSION_KEY);
        if (!hasText(activeColor) && !hasText(activeVersion)) {
            return chain.filter(exchange);
        }

        String serviceId = requestUrl.getHost();
        if (!hasText(serviceId)) {
            return chain.filter(exchange);
        }

        return discoveryClient.getInstances(serviceId)
                .collectList()
                .flatMap(instances -> filterAndRoute(exchange, chain, requestUrl, serviceId, instances, activeColor, activeVersion));
    }

    @Override
    public int getOrder() {
        return ORDER_BEFORE_GATEWAY_LOAD_BALANCER;
    }

    private Mono<Void> filterAndRoute(ServerWebExchange exchange, GatewayFilterChain chain, URI requestUrl,
                                      String serviceId, List<ServiceInstance> instances,
                                      String activeColor, String activeVersion) {
        ReleaseMatchResult matchResult = filterByRelease(instances, activeColor, activeVersion);
        if (matchResult.matchedInstances.isEmpty()) {
            log.warn("No active release instance found for gateway service={}, activeColor={}, activeVersion={}",
                    serviceId, activeColor, activeVersion);
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        ServiceInstance instance = choose(serviceId, matchResult.matchedInstances);
        URI releaseAwareUri = reconstructUri(requestUrl, instance);
        addOriginalRequestUrl(exchange, requestUrl);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, releaseAwareUri);
        return chain.filter(exchange);
    }

    private ReleaseMatchResult filterByRelease(List<ServiceInstance> instances, String activeColor, String activeVersion) {
        if (instances == null || instances.isEmpty()) {
            return new ReleaseMatchResult(Collections.emptyList());
        }

        List<ServiceInstance> matched = new ArrayList<>();
        for (ServiceInstance instance : instances) {
            Map<String, String> metadata = instance.getMetadata();
            if (metadata == null || metadata.isEmpty()) {
                continue;
            }
            String releaseColor = firstText(metadata, "release.color", "release-color", "releaseColor", "color");
            String releaseVersion = firstText(metadata, "release.version", "release-version", "releaseVersion", "version");
            if (matchesRelease(activeColor, activeVersion, releaseColor, releaseVersion)) {
                matched.add(instance);
            }
        }
        return new ReleaseMatchResult(matched);
    }

    private ServiceInstance choose(String serviceId, List<ServiceInstance> instances) {
        AtomicInteger position = positions.computeIfAbsent(serviceId, key -> new AtomicInteger(0));
        return instances.get(Math.floorMod(position.getAndIncrement(), instances.size()));
    }

    private URI reconstructUri(URI requestUrl, ServiceInstance instance) {
        URI instanceUri = instance.getUri();
        String scheme = instance.isSecure() ? "https" : "http";
        if (instanceUri != null && hasText(instanceUri.getScheme())) {
            scheme = instanceUri.getScheme();
        }
        return UriComponentsBuilder.fromUri(requestUrl)
                .scheme(scheme)
                .host(instance.getHost())
                .port(instance.getPort())
                .build(true)
                .toUri();
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class ReleaseMatchResult {
        private final List<ServiceInstance> matchedInstances;

        private ReleaseMatchResult(List<ServiceInstance> matchedInstances) {
            this.matchedInstances = matchedInstances;
        }
    }
}
