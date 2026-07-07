package com.common.business.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将本机发布标签写入 Nacos metadata，供 Gateway/Ribbon 的 release-aware 路由匹配。
 */
@Slf4j
@Component
public class NacosReleaseMetadataInitializer {

    private static final String RELEASE_COLOR_KEY = "release.color";
    private static final String RELEASE_VERSION_KEY = "release.version";
    private static final String SPRING_APPLICATION_NAME_KEY = "spring.application.name";

    private final NacosDiscoveryProperties discoveryProperties;
    private final Environment environment;

    public NacosReleaseMetadataInitializer(NacosDiscoveryProperties discoveryProperties, Environment environment) {
        this.discoveryProperties = discoveryProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        Map<String, String> metadata = new LinkedHashMap<>();
        Map<String, String> discoveryMetadata = discoveryProperties.getMetadata();
        if (discoveryMetadata != null && !discoveryMetadata.isEmpty()) {
            metadata.putAll(discoveryMetadata);
        }

        String color = firstText(metadata.get("color"), metadata.get("release-color"), metadata.get(RELEASE_COLOR_KEY),
                environment.getProperty("RELEASE_COLOR"), environment.getProperty(RELEASE_COLOR_KEY));
        String version = firstText(metadata.get("version"), metadata.get("release-version"),
                metadata.get(RELEASE_VERSION_KEY), environment.getProperty("RELEASE_VERSION"),
                environment.getProperty(RELEASE_VERSION_KEY));
        String serviceName = firstText(metadata.get("service"), metadata.get("serviceName"),
                metadata.get("service-name"), metadata.get(SPRING_APPLICATION_NAME_KEY),
                discoveryProperties.getService(), environment.getProperty(SPRING_APPLICATION_NAME_KEY));

        putReleaseAliases(metadata, color, version, serviceName);
        discoveryProperties.setMetadata(metadata);
        if (StringUtils.isNotBlank(color) || StringUtils.isNotBlank(version)) {
            log.info("Nacos release metadata initialized, service={}, color={}, version={}",
                    serviceName, color, version);
        }
    }

    private void putReleaseAliases(Map<String, String> metadata, String color, String version, String serviceName) {
        if (StringUtils.isNotBlank(color)) {
            metadata.put("color", color);
            metadata.put("release-color", color);
            metadata.put(RELEASE_COLOR_KEY, color);
        }
        if (StringUtils.isNotBlank(version)) {
            metadata.put("version", version);
            metadata.put("release-version", version);
            metadata.put(RELEASE_VERSION_KEY, version);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            metadata.put("service", serviceName);
            metadata.put("serviceName", serviceName);
            metadata.put("service-name", serviceName);
            metadata.put(SPRING_APPLICATION_NAME_KEY, serviceName);
        }
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
