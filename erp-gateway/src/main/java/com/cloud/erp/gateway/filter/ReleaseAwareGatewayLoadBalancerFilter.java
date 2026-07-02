package com.cloud.erp.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.ApplicationListener;
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
@ConditionalOnProperty(prefix = "release.gateway", name = "enabled", havingValue = "true")
public class ReleaseAwareGatewayLoadBalancerFilter
        implements GlobalFilter, Ordered, ApplicationListener<EnvironmentChangeEvent> {

    private static final Logger log = LoggerFactory.getLogger(ReleaseAwareGatewayLoadBalancerFilter.class);
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private static final String INSTANCE_CACHE_TTL_MS_KEY = "release.gateway.instance-cache-ttl-ms";
    private static final long DEFAULT_INSTANCE_CACHE_TTL_MS = 1000L;
    private static final int ORDER_BEFORE_GATEWAY_LOAD_BALANCER = 10050;

    private final ReactiveDiscoveryClient discoveryClient;
    private final Environment environment;
    private final ConcurrentHashMap<String, AtomicInteger> positions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedInstances> instanceCache = new ConcurrentHashMap<>();

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

        return getInstances(serviceId)
                .flatMap(instances -> filterAndRoute(exchange, chain, requestUrl, serviceId, instances, activeColor, activeVersion));
    }

    @Override
    public int getOrder() {
        return ORDER_BEFORE_GATEWAY_LOAD_BALANCER;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (event == null || event.getKeys() == null) {
            return;
        }
        if (event.getKeys().contains(ACTIVE_COLOR_KEY)
                || event.getKeys().contains(ACTIVE_VERSION_KEY)
                || event.getKeys().contains(INSTANCE_CACHE_TTL_MS_KEY)) {
            // 发布控制配置热刷新时立即丢弃发现缓存，避免切色窗口内继续使用旧实例快照。
            instanceCache.clear();
            log.warn("Cleared gateway release instance cache after release config changed: {}", event.getKeys());
        }
    }

    private Mono<List<ServiceInstance>> getInstances(String serviceId) {
        long ttlMs = getInstanceCacheTtlMs();
        if (ttlMs <= 0) {
            return discoveryClient.getInstances(serviceId).collectList();
        }

        long now = System.currentTimeMillis();
        CachedInstances cachedInstances = instanceCache.get(serviceId);
        if (cachedInstances != null && cachedInstances.expireAt > now) {
            return Mono.just(cachedInstances.instances);
        }

        return discoveryClient.getInstances(serviceId)
                .collectList()
                .doOnNext(instances -> instanceCache.put(serviceId,
                        new CachedInstances(Collections.unmodifiableList(new ArrayList<>(instances)),
                                System.currentTimeMillis() + ttlMs)));
    }

    private long getInstanceCacheTtlMs() {
        Long ttlMs = environment.getProperty(INSTANCE_CACHE_TTL_MS_KEY, Long.class, DEFAULT_INSTANCE_CACHE_TTL_MS);
        return ttlMs == null ? DEFAULT_INSTANCE_CACHE_TTL_MS : ttlMs;
    }

    private Mono<Void> filterAndRoute(ServerWebExchange exchange, GatewayFilterChain chain, URI requestUrl,
                                      String serviceId, List<ServiceInstance> instances,
                                      String activeColor, String activeVersion) {
        ReleaseMatchResult matchResult = filterByRelease(instances, activeColor, activeVersion);
        if (matchResult.matchedInstances.isEmpty()) {
            // filterByRelease 已对「无 release 元数据」的老实例做兼容回退；走到这里说明已检测到
            // release 元数据但没有命中 active 发布版本，网关侧返回 503，避免跨蓝绿版本转发。
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
        if (!hasText(activeColor) && !hasText(activeVersion)) {
            // 未配置发布规则时保持原始负载均衡行为，避免非蓝绿环境因实例已带标签而被误拦截。
            return new ReleaseMatchResult(instances);
        }

        List<ServiceInstance> matched = new ArrayList<>();
        boolean hasReleaseMetadata = false;
        for (ServiceInstance instance : instances) {
            Map<String, String> metadata = instance.getMetadata();
            if (metadata == null || metadata.isEmpty()) {
                // 仅当整个实例列表都没有 release 元数据时才兼容回退；混合场景下无标签实例不参与兜底，避免跨色转发。
                continue;
            }
            // 兼容存量部署已写入的裸 color/version；新实例会由 NacosReleaseMetadataInitializer 同步写入 release 前缀。
            String releaseColor = firstText(metadata, "release.color", "release-color", "releaseColor", "color");
            String releaseVersion = firstText(metadata, "release.version", "release-version", "releaseVersion", "version");
            if (hasAnyReleaseMetadata(releaseColor, releaseVersion)) {
                hasReleaseMetadata = true;
            }
            if (matchesRelease(activeColor, activeVersion, releaseColor, releaseVersion)) {
                matched.add(instance);
            }
        }
        if (matched.isEmpty() && !hasReleaseMetadata) {
            // 兼容未接入 release 元数据的存量实例：没有任何 release 标签时不强制切流。
            log.warn("No release metadata found for gateway activeColor={}, activeVersion={}, fallback to all instances",
                    activeColor, activeVersion);
            return new ReleaseMatchResult(instances);
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
        boolean hasActiveRule = false;
        if (hasText(activeColor)) {
            hasActiveRule = true;
            if (!hasText(releaseColor) || !activeColor.equalsIgnoreCase(releaseColor)) {
                return false;
            }
        }
        if (hasText(activeVersion)) {
            hasActiveRule = true;
            if (!hasText(releaseVersion) || !activeVersion.equalsIgnoreCase(releaseVersion)) {
                return false;
            }
        }
        return hasActiveRule;
    }

    private boolean hasAnyReleaseMetadata(String releaseColor, String releaseVersion) {
        return hasText(releaseColor) || hasText(releaseVersion);
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

    private static class CachedInstances {
        private final List<ServiceInstance> instances;
        private final long expireAt;

        private CachedInstances(List<ServiceInstance> instances, long expireAt) {
            this.instances = instances;
            this.expireAt = expireAt;
        }
    }
}
