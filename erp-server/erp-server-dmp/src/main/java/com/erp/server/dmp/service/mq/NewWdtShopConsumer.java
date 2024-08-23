package com.erp.server.dmp.service.mq;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_SHOP_TO_DMP_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_WDT_SHOP_TO_DMP_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_SHOP_TO_DMP_GROUP)
public class NewWdtShopConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private WdtShopConsumer wdtShopConsumer;

	@Override
	public String getBizName() {
		return "旺店通店铺";
	}
	
	@Override
	public void handle(String data) {
		wdtShopConsumer.handle(data);
	}
}