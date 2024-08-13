package com.erp.server.dmp.service.mq;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_WAREHOUSE_TO_DMP_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_WDT_WAREHOUSE_TO_DMP_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_WAREHOUSE_TO_DMP_GROUP)
public class NewWdtWarehouseConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private WdtWarehouseConsumer wdtWarehouseConsumer;

	@Override
	public String getBizName() {
		return "旺店通仓库";
	}
	
	@Override
	public void handle(String data) {
		wdtWarehouseConsumer.handle(data);
	}
}