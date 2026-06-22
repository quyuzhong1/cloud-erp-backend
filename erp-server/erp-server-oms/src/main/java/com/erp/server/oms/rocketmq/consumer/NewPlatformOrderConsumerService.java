package com.erp.server.oms.rocketmq.consumer;

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
 * 下载平台订单消费服务
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_ORDER_TO_OMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_ORDER_TO_OMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_ORDER_TO_OMS_GROUP,
consumeMode = ConsumeMode.CONCURRENTLY,
consumeThreadNumber = 8)
public class NewPlatformOrderConsumerService extends AbstractNewPlatformConsumerHandler{
	@Resource
	private PlatformOrderConsumerService platformOrderConsumerService;

	@Override
	public String getBizName() {
		return "销售平台订单";
	}
	
    @Override
	public void handle(String data) {
    	platformOrderConsumerService.handle(data);
	}

}