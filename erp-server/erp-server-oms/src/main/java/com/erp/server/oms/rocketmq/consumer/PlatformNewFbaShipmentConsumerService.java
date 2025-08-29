package com.erp.server.oms.rocketmq.consumer;

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
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FULFILL_ORDER_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FULFILL_ORDER_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FULFILL_ORDER_TO_OMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbaShipmentConsumerService extends AbstractNewPlatformConsumerHandler {

    @Override
    public String getBizName() {
        return "多渠道订单";
    }

    @Override
    public void handle(String data) {
        System.out.println(data);
//        platformFulfillOrderConsumerHandleService.handle(data);
    }

}