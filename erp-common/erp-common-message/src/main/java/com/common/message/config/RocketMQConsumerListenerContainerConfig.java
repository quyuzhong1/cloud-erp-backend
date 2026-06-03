package com.common.message.config;

import cn.hutool.core.util.ReflectUtil;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RockMQ消费者监听容器配置
 */
@Configuration
public class RocketMQConsumerListenerContainerConfig implements BeanPostProcessor, EnvironmentAware, ApplicationListener<ApplicationEvent> {

    private static final Logger log = LoggerFactory.getLogger(RocketMQConsumerListenerContainerConfig.class);
    private static final String MQ_CONSUMER_ENABLED_KEY = "release.mq.consumer.enabled";
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String LOCAL_COLOR_KEY = "release.color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private static final String LOCAL_VERSION_KEY = "release.version";
    private static final String ENVIRONMENT_CHANGE_EVENT = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";
    private final List<DefaultRocketMQListenerContainer> listenerContainers = new CopyOnWriteArrayList<>();
    private Environment environment;

    /**
     * 二次消费最大重试次数
     */
    @Value("${rocketmq.consumer.custom.max-reconsume-times:3}")
    private int maxReconsumeTimes;

    /**
     * 蓝绿发布时新版本先启动但不消费 MQ，避免新旧版本同时消费同一 ConsumerGroup。
     */
    @Value("${release.mq.consumer.enabled:true}")
    private boolean mqConsumerEnabled;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 在装载Bean之前实现参数修改
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultRocketMQListenerContainer) {
            Object fieldValueObj = ReflectUtil.getFieldValue(bean, "maxReconsumeTimes");
            if (fieldValueObj instanceof Integer) {
                // 默认重试消费5次
                if (-1 == (Integer) fieldValueObj) {
                    ReflectUtil.setFieldValue(bean, "maxReconsumeTimes", maxReconsumeTimes);
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof DefaultRocketMQListenerContainer)) {
            return bean;
        }

        DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) bean;
        listenerContainers.add(container);
        if (isMqConsumerEnabled()) {
            return bean;
        }

        log.warn("RocketMQ consumer container [{}] is disabled by release.mq.consumer.enabled=false", beanName);
        return Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                bean.getClass().getInterfaces(),
                new DisabledRocketMQConsumerInvocationHandler(container, beanName, this)
        );
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (ENVIRONMENT_CHANGE_EVENT.equals(event.getClass().getName())) {
            refreshMqConsumerState();
        }
    }

    private boolean isMqConsumerEnabled() {
        if (environment == null) {
            return mqConsumerEnabled;
        }
        return environment.getProperty(MQ_CONSUMER_ENABLED_KEY, Boolean.class, mqConsumerEnabled)
                && isCurrentReleaseActive();
    }

    private boolean isCurrentReleaseActive() {
        String activeColor = environment.getProperty(ACTIVE_COLOR_KEY);
        String localColor = environment.getProperty(LOCAL_COLOR_KEY);
        if (hasText(activeColor) && hasText(localColor)) {
            return activeColor.equalsIgnoreCase(localColor);
        }

        String activeVersion = environment.getProperty(ACTIVE_VERSION_KEY);
        String localVersion = environment.getProperty(LOCAL_VERSION_KEY);
        if (hasText(activeVersion) && hasText(localVersion)) {
            return activeVersion.equalsIgnoreCase(localVersion);
        }

        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void refreshMqConsumerState() {
        boolean enabled = isMqConsumerEnabled();
        for (DefaultRocketMQListenerContainer container : listenerContainers) {
            if (enabled && !container.isRunning()) {
                log.warn("Start RocketMQ consumer container [{}] by {}=true", container, MQ_CONSUMER_ENABLED_KEY);
                container.start();
            } else if (!enabled && container.isRunning()) {
                log.warn("Stop RocketMQ consumer container [{}] by {}=false", container, MQ_CONSUMER_ENABLED_KEY);
                container.stop();
            }
        }
    }

    private static class DisabledRocketMQConsumerInvocationHandler implements InvocationHandler {

        private final DefaultRocketMQListenerContainer target;
        private final String beanName;
        private final RocketMQConsumerListenerContainerConfig lifecycleConfig;

        private DisabledRocketMQConsumerInvocationHandler(DefaultRocketMQListenerContainer target,
                                                          String beanName,
                                                          RocketMQConsumerListenerContainerConfig lifecycleConfig) {
            this.target = target;
            this.beanName = beanName;
            this.lifecycleConfig = lifecycleConfig;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("isAutoStartup".equals(methodName)) {
                return false;
            }
            if ("isRunning".equals(methodName)) {
                return target.isRunning();
            }
            if ("start".equals(methodName)) {
                if (lifecycleConfig.isMqConsumerEnabled()) {
                    target.start();
                } else {
                    log.warn("Skip starting RocketMQ consumer container [{}]", beanName);
                }
                return null;
            }
            if ("stop".equals(methodName)) {
                if (args != null && args.length == 1 && args[0] instanceof Runnable) {
                    ((Runnable) args[0]).run();
                }
                return null;
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
