package com.erp.server.oms.rocketmq.consumer.restcloud;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.server.oms.rocketmq.consumer.NewPlatformReturnOrderConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2025/11/11 16:28
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class RestcloudPlatformReturnOrderConsumerService extends AbstractRestCloudPlatformConsumerHandler {
    @Resource
    private NewPlatformReturnOrderConsumerService platformReturnOrderConsumerService;

    @Override
    public String getBizName() {
        return "退货订单";
    }

    @Override
    public void handle(String data) {
        platformReturnOrderConsumerService.handle(data);
    }

}