package com.common.business.config;

import com.netflix.loadbalancer.IRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

/**
 * 全局启用 release-aware Ribbon 规则，覆盖 Feign 的 Nacos 实例选择。
 */
@Configuration
@ConditionalOnClass({IRule.class, RibbonClients.class})
@ConditionalOnProperty(prefix = "release.ribbon", name = "enabled", havingValue = "true", matchIfMissing = true)
@RibbonClients(defaultConfiguration = ReleaseAwareRibbonRuleConfiguration.class)
public class ReleaseAwareRibbonAutoConfiguration {
}
