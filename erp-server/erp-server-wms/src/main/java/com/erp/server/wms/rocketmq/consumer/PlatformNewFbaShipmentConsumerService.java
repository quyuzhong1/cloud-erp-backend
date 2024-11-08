package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 下载FBA货件消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBA_SHIPMENT_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FBA_SHIPMENT_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FBA_SHIPMENT_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbaShipmentConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private PlatformFbaShipmentConsumerService platformFbaShipmentConsumerService;

    @Override
    public String getBizName() {
        return "FBA货件";
    }

    @Override
    public void handle(String data) {
        platformFbaShipmentConsumerService.handle(data);
    }

}