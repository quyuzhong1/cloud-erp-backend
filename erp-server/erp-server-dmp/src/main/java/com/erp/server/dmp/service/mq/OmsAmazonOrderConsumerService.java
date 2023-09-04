package com.erp.server.dmp.service.mq;

import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台订单消费服务
 *
 * @Author Cloud
 * @Date 2023/8/31 17:10
 **/
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PUSH_DATA_TOPIC,
        selectorExpression = "demo",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-oms_push_amazon_order_consumer")
public class OmsAmazonOrderConsumerService<T> extends AbstractPlatformConsumerHandler<T> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(T ext) {

    }
}
