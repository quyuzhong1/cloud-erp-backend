package com.common.message.config;

import cn.hutool.core.util.ReflectUtil;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * RockMQ消费者监听容器配置
 */
@Configuration
public class RocketMQConsumerListenerContainerConfig implements BeanPostProcessor {

    /**
     * 二次消费最大重试次数
     */
    @Value("${rocketmq.consumer.custom.max-reconsume-times:3}")
    private int maxReconsumeTimes;

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
}
