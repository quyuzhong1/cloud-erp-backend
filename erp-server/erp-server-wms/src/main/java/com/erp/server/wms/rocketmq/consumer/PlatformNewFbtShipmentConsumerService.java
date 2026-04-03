package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.wms.rocketmq.consumer.handler.FbtShipmentMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * FBT货件消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBT_SHIPMENT_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FBT_SHIPMENT_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FBT_SHIPMENT_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbtShipmentConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private FbtShipmentMessageHandler fbtShipmentMessageHandler;

    @Override
    public String getBizName() {
        return "FBT货件";
    }

    @Override
    public void handle(String data) {
        fbtShipmentMessageHandler.handle(data);
    }
}
