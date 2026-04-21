package com.common.business.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.stream.Stream;

/**
 * 业务常量
 *
 * @author Jim
 * @date 2023/10/17 10:13
 */
@Component
public class BusinessCommonConstants {

    public static ZoneOffset systemZoneOffset =ZoneOffset.of("+8");

    // Spring 环境
    private static String SPRING_PROFILES_ACTIVE;
    
    // 命名空间
    private static String SPRING_NACOS_NAMESPACE;

    // 开发环境
    public static final String DEV = "dev";
    
    // 归档环境
    public static final String ARCHIVE = "archive";

    @Value("${spring.profiles.active:dev}")
    private void setSpringProfilesActive(String springProfilesActive) {
        BusinessCommonConstants.SPRING_PROFILES_ACTIVE = springProfilesActive;
    }
    
    @Value("${spring.cloud.nacos.discovery.namespace:dev}")
    private void setSpringNacosNamespace(String springNacosNamespace) {
    	BusinessCommonConstants.SPRING_NACOS_NAMESPACE = springNacosNamespace;
    }

    /**
     * 效验当前环境
     */
    public static boolean hasProfile(String profile) {
        String[] profiles = BusinessCommonConstants.SPRING_PROFILES_ACTIVE.split(",");
        return Stream.of(profiles).map(String::trim).anyMatch(p -> p.equals(profile));
    }

    /**
     * 效验当前环境
     */
    public static String  getEnvironment() {
        String[] profiles = BusinessCommonConstants.SPRING_PROFILES_ACTIVE.split(",");
        return profiles[0];
    }

    /**
     * 效验是否归档环境
     */
    public static boolean isArchive() {
        return SPRING_NACOS_NAMESPACE.toLowerCase().contains(ARCHIVE);
    }
}
