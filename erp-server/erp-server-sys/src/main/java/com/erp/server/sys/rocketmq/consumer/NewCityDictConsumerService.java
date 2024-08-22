package com.erp.server.sys.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 下载第三方城市字典消费类
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_REGION_TO_SYS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_REGION_TO_SYS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_REGION_TO_SYS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class NewCityDictConsumerService extends AbstractNewPlatformConsumerHandler {
	@Resource
	private CityDictConsumerService cityDictConsumerService;
	
	@Override
	public String getBizName() {
		return "第三方区域";
	}

	@Override
	public void handle(String data) {
		cityDictConsumerService.handle(data);
	}

}
