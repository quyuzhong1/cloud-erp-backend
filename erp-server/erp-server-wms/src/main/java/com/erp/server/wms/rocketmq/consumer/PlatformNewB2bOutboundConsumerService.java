package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2B三方仓出库状态消费入口
 */
@Service
@RocketMQMessageListener(
        topic = RocketMqNewTopic.DMP_PLATFORM_B2B_THIRD_OUTBOUND_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_B2B_THIRD_OUTBOUND_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_B2B_THIRD_OUTBOUND_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewB2bOutboundConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private B2bThirdOutboundConsumerService b2bThirdOutboundConsumerService;

    @Override
    public String getBizName() {
        return "B2B三方仓出库状态";
    }

    @Override
    public void handle(String data) {
        b2bThirdOutboundConsumerService.handle(data);
    }
}
