package com.common.message.config;

import com.common.message.trace.IndependentTraceRocketMQListenerAdvice;
import com.common.message.trace.IndependentTraceRunner;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 为全部 {@link RocketMQMessageListener} 消费者包装独立 Trace 入口，使每条消息消费生成新的 traceId。
 * <p>
 * 使用 CGLIB 子类代理拦截 {@code onMessage}，保留原 Bean 具体类型，避免
 * {@code @Resource SyncXxxConsumer} 等按类型注入出现 {@code BeanNotOfRequiredTypeException}。
 * <p>
 * <b>部署侧必须配置</b>（SkyWalking Java Agent 9.6.x，插件名以 agent {@code plugins/} 目录为准）：
 * <pre>
 * -Dplugin.exclude_plugins=rocketmq-4.x-plugin
 * </pre>
 * 或环境变量 {@code SW_EXCLUDE_PLUGINS=rocketmq-4.x-plugin}。
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
        if (!(bean instanceof RocketMQListener)) {
            return bean;
        }
        if (alreadyWrapped(bean)) {
            return bean;
        }
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (AnnotationUtils.findAnnotation(targetClass, RocketMQMessageListener.class) == null) {
            return bean;
        }
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new IndependentTraceRocketMQListenerAdvice(traceRunner));
        return proxyFactory.getProxy();
    }

    private static boolean alreadyWrapped(Object bean) {
        if (!(bean instanceof Advised)) {
            return false;
        }
        for (Advisor advisor : ((Advised) bean).getAdvisors()) {
            if (advisor.getAdvice() instanceof IndependentTraceRocketMQListenerAdvice) {
                return true;
            }
        }
        return false;
    }
}
