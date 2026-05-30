package com.common.business.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 配合 spring.cloud.nacos.discovery.instance-enabled=false 使用：
 * 新实例先注册为不可用，应用完全 ready 后再允许 Nacos 调用方发现。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class NacosRegistrationLifecycle {

    @Resource
    private NacosSelfRegistrationChecker nacosSelfRegistrationChecker;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        boolean enabled = nacosSelfRegistrationChecker.setSelfEnabled(true);
        if (enabled) {
            log.info("Current Nacos instance has been marked enabled after application ready");
        }
    }
}
