package com.common.business.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * 业务常量
 *
 * @author Jim
 * @date 2023/10/17 10:13
 */
@Component
public class BusinessCommonConstants {

    // Spring 环境
    private static String SPRING_PROFILES_ACTIVE;

    @Value("${spring.profiles.active:dev}")
    private void setSpringProfilesActive(String springProfilesActive) {
        BusinessCommonConstants.SPRING_PROFILES_ACTIVE = springProfilesActive;
    }

    /**
     * 效验当前环境
     */
    public static boolean hasProfile(String profile) {
        String[] profiles = BusinessCommonConstants.SPRING_PROFILES_ACTIVE.split(",");
        return Stream.of(profiles).map(String::trim).anyMatch(p -> p.equals(profile));
    }


}
