package com.erp.server.dmp.service.mq;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 领星店铺消费
 */
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_LX_SHOP_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_LX_SHOP_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_LX_SHOP_TO_DMP_GROUP)
public class NewLxShopConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private MQLingxingConsumerService.ConsumerErpShopInfo consumerErpShopInfo;

    @Override
    public String getBizName() {
        return "领星店铺";
    }

    @Override
    public void handle(String data) {
        consumerErpShopInfo.onMessage(data);
    }
}