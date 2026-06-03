package com.common.business.config;

import com.netflix.loadbalancer.IRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 全局启用 release-aware Ribbon 规则，覆盖 Feign 的 Nacos 实例选择。
 */
@Configuration
@ConditionalOnClass({IRule.class, RibbonClients.class})
@RibbonClients(defaultConfiguration = ReleaseAwareRibbonAutoConfiguration.ReleaseAwareRibbonRuleConfiguration.class)
public class ReleaseAwareRibbonAutoConfiguration {

    @Configuration
    public static class ReleaseAwareRibbonRuleConfiguration {

        @Bean
        @ConditionalOnMissingBean(IRule.class)
        public IRule releaseAwareRibbonRule(Environment environment) {
            return new ReleaseAwareRibbonRule(environment);
        }
    }
}
