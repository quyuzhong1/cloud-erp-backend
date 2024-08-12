package com.erp.server.tms.rocketmq;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_TRACK123_TO_TMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_TRACK123_TO_TMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_TRACK123_TO_TMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class NewDmpPlatformTrackConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private PlatformTrackConsumerService platformTrackConsumerService;

    @Override
    public String getBizName() {
        return "物流轨迹信息";
    }

    @Override
    public void handle(String data) {
        platformTrackConsumerService.handle(data);
    }
}
