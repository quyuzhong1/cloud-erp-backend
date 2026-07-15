package com.common.message.config;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.mock.env.MockEnvironment;

public class RocketMQConsumerBootstrapPostProcessorTest {

    private static final String LISTENER_CONFIGURATION_BEAN = "listenerContainerConfiguration";
    private static final String OTHER_BEAN = "otherBean";

    @Test
    public void shouldRemoveOnlyListenerConfigurationWhenConsumerDisabled() {
        DefaultListableBeanFactory registry = registry();
        RocketMQConsumerBootstrapPostProcessor postProcessor = postProcessor(false);

        postProcessor.postProcessBeanDefinitionRegistry(registry);

        Assert.assertFalse(registry.containsBeanDefinition(LISTENER_CONFIGURATION_BEAN));
        Assert.assertTrue(registry.containsBeanDefinition(OTHER_BEAN));
    }

    @Test
    public void shouldKeepListenerConfigurationWhenConsumerEnabled() {
        DefaultListableBeanFactory registry = registry();
        RocketMQConsumerBootstrapPostProcessor postProcessor = postProcessor(true);

        postProcessor.postProcessBeanDefinitionRegistry(registry);

        Assert.assertTrue(registry.containsBeanDefinition(LISTENER_CONFIGURATION_BEAN));
    }

    @Test
    public void shouldEnableConsumerByDefault() {
        DefaultListableBeanFactory registry = registry();
        RocketMQConsumerBootstrapPostProcessor postProcessor = new RocketMQConsumerBootstrapPostProcessor();
        postProcessor.setEnvironment(new MockEnvironment());

        postProcessor.postProcessBeanDefinitionRegistry(registry);

        Assert.assertTrue(registry.containsBeanDefinition(LISTENER_CONFIGURATION_BEAN));
    }

    private DefaultListableBeanFactory registry() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        registry.registerBeanDefinition(LISTENER_CONFIGURATION_BEAN,
                new RootBeanDefinition(ListenerContainerConfiguration.class));
        registry.registerBeanDefinition(OTHER_BEAN, new RootBeanDefinition(String.class));
        return registry;
    }

    private RocketMQConsumerBootstrapPostProcessor postProcessor(boolean enabled) {
        RocketMQConsumerBootstrapPostProcessor postProcessor = new RocketMQConsumerBootstrapPostProcessor();
        postProcessor.setEnvironment(new MockEnvironment().withProperty(
                RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, Boolean.toString(enabled)));
        return postProcessor;
    }
}
