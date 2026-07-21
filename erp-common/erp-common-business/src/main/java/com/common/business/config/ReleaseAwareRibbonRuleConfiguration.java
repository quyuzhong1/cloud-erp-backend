package com.common.business.config;

import com.netflix.loadbalancer.IRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Ribbon client 子上下文配置；不要注册到主 Spring 上下文，避免多个服务共享同一个 IRule。
 */
public class ReleaseAwareRibbonRuleConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = IRule.class, search = SearchStrategy.CURRENT)
    public IRule releaseAwareRibbonRule(Environment environment) {
        return new ReleaseAwareRibbonRule(environment);
    }
}
