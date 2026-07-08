package com.common.message.config;

import com.common.message.trace.IndependentTraceRocketMQListenerWrapper;
import com.common.message.trace.IndependentTraceRunner;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 为全部 {@link RocketMQMessageListener} 消费者包装独立 Trace 入口，使每条消息消费生成新的 traceId。
 * <p>
 * <b>部署侧必须配置</b>（SkyWalking Java Agent 9.6.x，插件名以 agent {@code plugins/} 目录为准）：
 * <pre>
 * -Dplugin.exclude_plugins=rocketmq-4.x-plugin
 * </pre>
 * 否则 Agent 会在进入 {@code onMessage} 前解析消息中的 SW8 上下文，{@code @Trace} 无法真正换新 traceId。
 * <p>
 * 启用后发送侧也不再注入 SW8，HTTP/Feign 与 MQ 消费在 SkyWalking UI 中不再自动串联；如需人工关联可在消息体中携带业务 traceId。
 */
@Configuration
public class IndependentTraceRocketMQConsumerConfig implements BeanPostProcessor {

    private final IndependentTraceRunner traceRunner;

    public IndependentTraceRocketMQConsumerConfig(IndependentTraceRunner traceRunner) {
        this.traceRunner = traceRunner;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IndependentTraceRocketMQListenerWrapper) {
            return bean;
        }
        if (!(bean instanceof RocketMQListener)) {
            return bean;
        }
        if (AnnotationUtils.findAnnotation(bean.getClass(), RocketMQMessageListener.class) == null) {
            return bean;
        }
        return new IndependentTraceRocketMQListenerWrapper((RocketMQListener) bean, traceRunner);
    }
}
