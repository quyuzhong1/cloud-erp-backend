package com.common.message.config;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Controls whether RocketMQ listener containers are registered for this process.
 * Producers and RocketMQTemplate remain available when consumers are disabled.
 */
@Component
public class RocketMQConsumerBootstrapPostProcessor
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {

    public static final String CONSUMER_ENABLED_PROPERTY = "erp.mq.consumer.enabled";

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerBootstrapPostProcessor.class);
    private static final String LISTENER_CONFIGURATION_CLASS = ListenerContainerConfiguration.class.getName();

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (isConsumerEnabled()) {
            return;
        }

        int removed = 0;
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (isListenerContainerConfiguration(beanDefinition)) {
                registry.removeBeanDefinition(beanName);
                removed++;
            }
        }

        if (removed == 0) {
            LOGGER.warn("RocketMQ consumers are disabled, but ListenerContainerConfiguration was not registered");
            return;
        }
        LOGGER.info("RocketMQ consumers are disabled; removed {} listener container configuration bean(s)", removed);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No bean instances are changed. Removing the listener registrar is sufficient.
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    boolean isConsumerEnabled() {
        if (environment == null) {
            return true;
        }

        String configuredValue = environment.getProperty(CONSUMER_ENABLED_PROPERTY);
        if (configuredValue == null || configuredValue.trim().isEmpty()) {
            return true;
        }
        if ("true".equalsIgnoreCase(configuredValue.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(configuredValue.trim())) {
            return false;
        }

        // A malformed release value must not crash the whole service or start duplicate consumers.
        LOGGER.error("Invalid {} value '{}'; RocketMQ consumers will remain disabled",
                CONSUMER_ENABLED_PROPERTY, configuredValue);
        return false;
    }

    private boolean isListenerContainerConfiguration(BeanDefinition beanDefinition) {
        if (LISTENER_CONFIGURATION_CLASS.equals(beanDefinition.getBeanClassName())) {
            return true;
        }
        Class<?> resolvedType = beanDefinition.getResolvableType().resolve();
        return resolvedType != null && LISTENER_CONFIGURATION_CLASS.equals(resolvedType.getName());
    }
}
