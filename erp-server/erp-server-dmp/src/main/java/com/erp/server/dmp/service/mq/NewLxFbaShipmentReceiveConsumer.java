package com.erp.server.dmp.service.mq;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 新中台-领星FBA货件签收消费
 */
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_GROUP)
public class NewLxFbaShipmentReceiveConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private MQLingxingConsumerService.ConsumerErpFbaReceive consumerErpFbaReceive;

    @Override
    public String getBizName() {
        return "领星FBA货件";
    }

    @Override
    public void handle(String data) {
        consumerErpFbaReceive.onMessage(data);
    }
}