package com.common.message.handler;

import com.common.message.constant.RocketMqTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 销售订单处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_data_consumer")
public abstract class AbstractPlatformConsumerHandler<T> implements RocketMQListener<T> {

    @Override
    public void onMessage(T ext) {
        try {
            handle(ext);
        }catch (Exception e) {
            log.error("平台数据消费异常", e);
        }
    }

    /**
     * 处理平台数据
     * @param ext
     */
    public abstract void handle(T ext);

}