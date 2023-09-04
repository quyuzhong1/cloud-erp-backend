package com.erp.server.oms.rocketmq;

import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * 下载平台订单消费服务
 * @author Cloud
 */
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "demo",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_amazon_order_consumer")
public class PlatformAmazonOrderConsumerService<T> extends AbstractPlatformConsumerHandler<T> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(T ext) {

    }
}
