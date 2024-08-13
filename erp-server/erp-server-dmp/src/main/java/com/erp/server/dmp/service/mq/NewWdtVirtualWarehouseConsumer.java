package com.erp.server.dmp.service.mq;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_VIRTUALWAREHOUSE_TO_DMP_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_WDT_VIRTUALWAREHOUSE_TO_DMP_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_VIRTUALWAREHOUSE_TO_DMP_GROUP)
public class NewWdtVirtualWarehouseConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private WdtVirtualWarehouseConsumer wdtVirtualWarehouseConsumer;

	@Override
	public String getBizName() {
		return "旺店通虚拟仓库";
	}
	
	@Override
	public void handle(String data) {
		wdtVirtualWarehouseConsumer.handle(data);
	}
}